## Setup
```
git clone https://github.com/gretard/junitcontest.git
sudo docker run -v /vagrant/junitcontest:/home/junit --name=junitcontest -it dockercontainervm/junitcontest:test
```
## docker
```
apt-get update
apt-get -y install git maven wget
```
## projects setup
```
cd /var/benchmarks/projects
rm -R *
git clone https://github.com/JodaOrg/joda-time.git
git clone https://github.com/JodaOrg/joda-money.git
git clone https://github.com/jfree/jfreechart.git
git clone https://github.com/jhy/jsoup.git
```

## compile
```
for i in */; do
  newdir="./${i}/pom.xml"
  mvn -DskipTests install dependency:copy-dependencies -f $newdir
done
```


# Setup custom benchmarks
```
cd /home/junit
mvn clean install -f ./src/benchgenerator/pom.xml
```
## Generate classes
```
java -jar "./src/benchgenerator/target/benchgenerator-0.0.1-SNAPSHOT-jar-with-dependencies.jar" "/var/benchmarks/projects" "/var/benchmarks/conf/benchmarks.list" 9999 true /home/junit/tools/project-stats.csv false
```
## Generate test classes for augmenting
```
java -jar "./src/benchgenerator/target/benchgenerator-0.0.1-SNAPSHOT-jar-with-dependencies.jar" "/var/benchmarks/projects" "/var/benchmarks/conf/benchmarks.list" 9999 true /home/junit/tools/project-stats.csv true
```
# Setup pit runner for analysis
```
cd /home/junit
mvn clean install -f ./src/pit-runner/pom.xml
cp ./src/pit-runner/target/*.jar ./tools/

```



## Benchmark tool
```
cd /home/junit

mvn -DskipTests clean install -f ./src/benchmarktool/pom.xml
cp ./src/benchmarktool/target/benchmarktool-1.0.0-shaded.jar /usr/local/bin/lib
```

## Universal test run tool
```
cd /home/junit;
mvn -DskipTests clean install -f ./src/sbstcontest/pom.xml

mkdir ./tools/atg/lib;
mkdir ./tools/evosuite/lib;
mkdir ./tools/randoop/lib;
cp ./src/sbstcontest/target/runtool-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./tools/atg/lib;
cp ./src/sbstcontest/target/runtool-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./tools/evosuite/lib;
cp ./src/sbstcontest/target/runtool-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./tools/randoop/lib;

```


## Evosuite setup
```
cd /home/junit

mkdir ./tools/evosuite/lib
wget https://github.com/EvoSuite/evosuite/releases/download/v1.0.6/evosuite-1.0.6.jar  -O ./tools/evosuite/lib/evosuite.jar
cp ./src/sbstcontest/target/runtool-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./tools/evosuite/lib
```
## Randoop setup
```
cd /home/junit

mkdir ./tools/randoop/lib
wget https://github.com/randoop/randoop/releases/download/v4.1.0/randoop-all-4.1.0.jar -O ./tools/randoop/lib/randoop.jar
cp ./src/sbstcontest/target/runtool-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./tools/randoop/lib

```

## Running experiments
```
docker start junitcontest
docker attach junitcontest
cd /home/junit/tools/atg


cd /home/junit/tools/atg; 
contest_generate_tests.sh atg 1 1 30;
cd /home/junit/tools/evosuite;
contest_generate_tests.sh evosuite 1 1 30;
cd /home/junit/tools/randoop;
contest_generate_tests.sh randoop 1 1 30;

cd /home/junit/tools;
java -jar runner-0.0.1-SNAPSHOT-jar-with-dependencies.jar;
```


```
contest_generate_tests.sh atg 1 1 30
contest_compute_metrics.sh results_atg_30 
contest_transcript_single.sh .

```

```
mkdir /home/junit/libs
cd /home/junit/libs
wget https://github.com/hcoles/pitest/releases/download/pitest-parent-1.4.5/pitest-1.4.5.jar
wget https://github.com/hcoles/pitest/releases/download/pitest-parent-1.4.5/pitest-command-line-1.4.5.jar
wget https://github.com/hcoles/pitest/releases/download/pitest-parent-1.4.5/pitest-entry-1.4.5.jar


cd /home/junit/tools
java -jar runner-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

```
select 
COLUMNS[1] as tool,
COLUMNS[2] as budget,
COLUMNS[4] as classz,
COLUMNS[5] as mutationType,
COLUMNS[5] || '_' || COLUMNS[4] || '_' || COLUMNS[6] ||  '_' || COLUMNS[7] as mutationId,
COLUMNS[8] as status
from dfs.`/home/junit/tools/summary.txt`
```
