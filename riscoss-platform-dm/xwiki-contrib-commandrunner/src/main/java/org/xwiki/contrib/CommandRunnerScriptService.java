/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.script.service.ScriptService;
import org.xwiki.component.annotation.Component;

@Component
@Named("commandRunner")
public class CommandRunnerScriptService implements ScriptService
{
    @Inject
    private DocumentAccessBridge bridge;

    private static class CmdReturn
    {
        String stdout = "";
        String stderr = "";

        static final int retCode_TIMEOUT = (1<<31);
        int retCode;
    }

    private static class Func {
        void call() throws Exception { }
    }

    private static Runnable startThread(final AtomicInteger ai, final Func f)
    {
        ai.incrementAndGet();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    f.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    ai.decrementAndGet();
                }
            }
        });
        t.start();
        return t;
    }

    private static CmdReturn runCmd(final String cmd,
                                    final String stdin,
                                    long timeoutMillis,
                                    final String wrkDir)
        throws Exception
    {
        //System.out.println("debug: " + cmd + " < " + stdin);

        final CmdReturn out = new CmdReturn();
        final AtomicInteger ai = new AtomicInteger(0);
        final Process[] process = new Process[1];

        startThread(ai, new Func() { void call() throws Exception {
            process[0] =
                new ProcessBuilder(Arrays.asList(cmd.split(" "))).directory(new File(wrkDir)).start();
            startThread(ai, new Func() { void call() throws Exception {
                out.stdout = IOUtils.toString(process[0].getInputStream(), "UTF-8");
            }});
            startThread(ai, new Func() { void call() throws Exception {
                out.stderr = IOUtils.toString(process[0].getErrorStream(), "UTF-8");
            }});
            startThread(ai, new Func() { void call() throws Exception {
                OutputStream stdinStream = process[0].getOutputStream();
                IOUtils.write(stdin, stdinStream, "UTF-8");
                stdinStream.close();
            }});

            process[0].waitFor();
            if (out.retCode != CmdReturn.retCode_TIMEOUT) {
                out.retCode = process[0].exitValue();
            }
        }});


        int waitMilliseconds = 0;
        while (ai.get() != 0) {
            Thread.sleep(10);
            waitMilliseconds += 10;
            if (waitMilliseconds > timeoutMillis) {
                if (process[0] != null) {
                    process[0].destroy();
                }
                //System.out.println("warning: TIMEOUT " + cmd + " < " + stdin);
                out.retCode = CmdReturn.retCode_TIMEOUT;
            }
            if (waitMilliseconds > (2 * timeoutMillis)) {
                throw new RuntimeException("hung thread");
            }
        }
        return out;
    }

    public Map<String, String> run(String command, String stdin, long timeoutMillis, String wrkDir)
        throws Exception
    {
        Map<String, String> outMap = new HashMap<String, String>();
        if (!this.bridge.hasProgrammingRights()) {
            outMap.put("error", "ENOPERM");
            return outMap;
        }

        CmdReturn out = runCmd(command, stdin, timeoutMillis, wrkDir);
        outMap.put("stdout", out.stdout);
        outMap.put("stderr", out.stderr);
        if (out.retCode == CmdReturn.retCode_TIMEOUT) {
            outMap.put("error", "TIMEOUT");
            out.retCode = -1;
        } else {
            outMap.put("error", "none");
        }
        outMap.put("ret", ""+out.retCode);
        return outMap;
    }

    public Map<String, String> run(String command, String stdin, long timeoutMillis)
        throws Exception
    {
        return run(command, stdin, timeoutMillis, System.getProperty("user.dir"));
    }
}
