package eu.riscoss.rdrpost;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    public static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        try {
            Options options = new Options();
            Option rdrOption = OptionBuilder.hasArg().withArgName("rdrURI").create("rdr");
            options.addOption(rdrOption);

            CommandLineParser commandLineParser = new GnuParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            if (!commandLine.hasOption("rdr")) {
                LOGGER.error("No rdr parameter specified");
                System.exit(1);
            }

            String line = null;
            StringBuffer sb = new StringBuffer();
            boolean dataStarted = false;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while ((line = br.readLine()) != null) {
                LOGGER.info(String.format("Got: %s", line));

                if (line.contains("END RISK DATA")) {
                    dataStarted = false;
                }

                if (dataStarted) {
                    sb.append(line);
                }

                if (line.contains("BEGIN RISK DATA")) {
                    dataStarted = true;
                }
            }

            String rdr = commandLine.getOptionValue("rdr");
            String data = sb.toString();

            LOGGER.info(String.format("Posting to %s: '%s'", rdr, data));

            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(rdr);
            post.setHeader("Content-type", "application/json");
            post.setEntity(new StringEntity(data));
            CloseableHttpResponse response = client.execute(post);

            LOGGER.info(String.format("RDR replied: %s", response.getStatusLine().toString()));

            client.close();
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }
    }
}
