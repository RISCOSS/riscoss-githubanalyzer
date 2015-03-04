# Risk Data Format

Risk drivers are represented as one of three data types, `NUMBER`, `DISTRIBUTION`
and `EVIDENCE`.

There is no such think as "risk data format". RISCOSS uses terminology of data elements, risk drivers, risk indicators, business risks, evidence. 

#### Data Elements
Raw data retrieved from various data sources, i.e. github development logs, fossology, email log, chat log
Data can be collected periodicatly or ad-hoc.
Data can be collected for any time windows: an entire year, one month etc.
Data can be stored "as is" within RISCOSS repositoy (versioning is required?) and/or its derived distribution lavels

#### Distribution of Risk Drivers
A Bayesian risk model is based on distribution of data values over several categories/groups/levels/discretizations (all synonimous for our purposes). The initial Bayesian setup process determins either statistically or by expert assessments what are the relevant groups for a risk driver. Than the raw data is analyzed resulting in distribution levels over the defined group levels. These distributios are used to create Bayesian networks that will become the Bayesian risk model.

Rules for setting up distributions (initial step performed off-line):

1. Raw data is summed up on a daily basis. E.g. collecting bug information or license blocking issues, data will be collected as "element per day," i.e. number of bugs per day.
3. Analyze bugs per day data and determine the distribution groups (typically 3 to 5 levels)
4. Calculate distributions of bugs per day over the distribution levels (akin to generating bar charts over predetermined bins)
5. Define relationships between risk drivers and risk indicators: either via expert opinion obtain in a tactical workshop or calculated if information is available
6. Define relationships between risk indicators and contextual indicators to business risks, by experts participating in a trategic workshop
7. Create the Bayesian model comprised of Bayesian networks for each risk indicator and one for business risk
8. Deploy Bayesian files (xdsl) into RISCOSS platform

Rules for running data collection to perform risk analysis:

1. Collect raw data  aggregated on daily basis
2. Calculate distributions of bugs per day over the predefined distribution levels (akin to generating bar charts over predetermined bins)
3. Distribution can represent single value, snap shot of OSS infor
4. Distribution can represent flactuations over time, i.e. the period for whcih data was collected
5. For the former: distribution over the Bayesian groups will be: 100% for the relevant level and 0% for all others
6. For the latter: distribution over the Bayesian groups will reflect different percentages of values (totaling 100%)

## NUMBER

`NUMBER` is a simple integer or decimal value based on it's meaning.

## DISTRIBUTION

A `DISTRIBUTION` is an array of decimal numbers, the sum of these numbers must be equal to one.
TODO: **I have no idea what the values of these numbers mean**

An 'EVIDENCE' data type is a pair of values [positive, negative], where the 'positive' part indicates the truth value of a certain fact, while the 'negative' part indicate its falsity degree. E.g., the sentence "today there was a good weather" can be true (positive evidence) with a value 0.8, because most of the day it was sunny, but it can be false (negative evidence) with a value 0.7 if we consider that around nood there has been a strong storm. So the two values can hold at the same time.

Out of the two values, some other significant information can be calculated:

* **direction**: real number in the range [-1,1], which summarizes the balancing of positive and negative values. 
Calculated as: positive - negative.
So in the "good weather example", overall we can say that positive evidence slightly overcomes the onegative one, so the direction is +0.1.
In the output gauge it is reflected in the rotation angle of the white circle around the center of the gauge.

* **conflict**: when positive and negative evidence hold at the same time, this value indicates the degree to which they overlap. 
Calculated as: max(positive, negative) - abs(positive - negative)
So in the "good weather example", we can say that the negative evidence erodes most of the positive one; we say that the conflict value is 0.7.
In the output gauge it is reflected in the shadowed ring around the white circle.

* **strength**: summarizes in une sigle number the everall amount of information carried by positive and negative values. 
Calculated as: abs( positive + negative - positive * negative )
In the output gauge it is refleted in the distance between the white circle and the center of the gauge.

* **signal**: normalizes positive and negative values in one real in the range [0,1].
Calculated as: (1 + direction) /2
Currently not visualized in the output gauge.


#Risk data "architecture"

The architecture in short:
* The risk models contain, as the lowest level, the "indicators".
* These indicators need to appear in the Risk Data Repository (RDR)
* The RDR is filled from independent programs called Risk Data Collectors (RDC)
* The RDCs have thus the task to load correctly formatted and computed data into the RDR. 
* This data can be obtained from single or multiple measures, statistics, other databases, etc., i.e. a RDC can directly monitor data, or alternatively rely on some database which may be in- or outside the RISCOSS boundary. E.g. there could be some tool (which could be an RDC, too) which creates a local database of JIRA issue metadata, which is queried by an RDC to create the requested combined measures. Also, a RDC could read data from RDR to compute new indicators.
* The RDR should *not* be misused as a database of high numbers of measures that are not used as indicators.
* Who creates a risk model needs to ensure that indicators are available.
* A more complex protocol for synchronising RDCs (RDR) and the models used for evaluation is not yet elaborated.

#Documentation of Risk Data instances
Data currently available for OW2 use cases - spreadsheet by Cedric (OW2) and Ron (KPA): https://docs.google.com/spreadsheets/d/1tR7v82N2Q13TGhS4CkQPcFT4GxcET4zR_AWjA75HSk8/edit?usp=sharing

The risk data measures presented in this spreadsheet are quite self explaining. All the data used for now can be obtained one-shot, i.e. by looking at a single (e.g. the current) time. This would not be the case for other measures like "commits on a holiday" or "new issues created" would need a temporal interval for computation of a single value, i.e. 1 week, 1 year, etc., and another, much larger, interval for creating a distribution. If this is the case, the according documentation needs to be added; a currently used, correct example is: *DS.4.b	Contributors in the last 12 months*.

1) Fossology data: 
ok

2) Sonar data:
there is a lot of data available. The actual provider gives various examples, and is configurable by properties (see the original .properties files).

3) OMM: 
"Number of 1 in" ... means the number of "1" in the evaluation form.

4) JIRA:
not contained in the actual spreadsheet, but prepares already the data needed by the older KPA activeness network.



Information on the original risk data providers and on the data currently available on the Risk Data Repository (RDR)
https://drive.google.com/file/d/0BycHQRiKncxHYnA5TEVlelVxU1k/view?usp=sharing
