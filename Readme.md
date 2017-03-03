By default, crawler will scan wiprodigital.com website, but website can also be passed as runtime argument.
We want to capture all urls (and follow the rules in robots.txt), including those, that don't start with http/s or have www. So need to make sure regular expression would capture all this cases.
Maven build (mvn clean install) would run the tests and build an executable jar. Once it's executed, a json file with website's name would be create/overriden in parent directory.
Also we don't care about anchor links or query parametes while searching for urls, so there is a set of spechial characters that we want to split on. And another set for urls that we already visited.
Four tests would run during the maven build, three are negative and one basic positive.