## Setup
```
git clone https://github.com/gretard/junitcontest.git
sudo docker run -v ./junitcontest:/home/junit --name=junitcontest -it dockercontainervm/junitcontest:test
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
java -jar "./src/benchgenerator/target/benchgenerator-0.0.1-SNAPSHOT-jar-with-dependencies.jar" "/var/benchmarks/projects" "/var/benchmarks/conf/benchmarks.list"
```



## Benchmark tool
```
cd /home/junit

mvn -DskipTests clean install -f ./src/benchmarktool/pom.xml
cp ./src/benchmarktool/target/benchmarktool-1.0.0-shaded.jar /usr/local/bin/lib
```

## Universal test run tool
```
cd /home/junit

mvn -DskipTests clean install -f ./src/sbstcontest/pom.xml
mkdir ./tools/atg/lib
cp ./src/sbstcontest/target/runtool-1.0.0-SNAPSHOT-jar-with-dependencies.jar ./tools/atg/lib
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
https://github.com/randoop/randoop/releases/download/v4.1.0/randoop-all-4.1.0.jar -O ./tools/randoop/lib/randoop.jar
mvn -DskipTests clean install dependency:copy-dependencies -f ./tools/randoop/sbstcontest/pom.xml
cp ./tools/randoop/sbstcontest/target/dependency/* ./tools/randoop/lib/dependency
```

## Running experiments
```
cd /home/junit/tools/TOOL


contest_generate_tests.sh atg 1 1 30
contest_compute_metrics.sh results_atg_30 
contest_transcript_single.sh .
```
