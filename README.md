jMetrik
=======


jMetrik is a free and open source computer program for psychometric analysis. It is a pure Java application that features a user-friendly interface, integrated database, and a variety of statistical procedures and charts. The interface is intuitive and easy to learn. It also scales to the experience of the user. New users can quickly learn to implement psychometric procedures though point-and-click menus. Experienced users can take advantage of the jMetrik command structure and write command files for executing an analysis.

jMetrik’s embedded database increases productivity by providing a common data format for all of its methods. There is no need to reformat or reshape data for each procedure. The database is the primary mechanism for data management. There is virtually no limit to the sample size or number of tables that can be stored in the database. Users are only limited by the amount of storage on their computer. After importing data into jMetrik, users can create subsets of data by selecting examinees or variables. Users can also create new tables by saving the results of an analysis in the database for further processing.



The compiled jMetrik application can be downloaded from <a href="http://www.ItemAnalysis.com">http://www.ItemAnalysis.com</a>.

jMetrik involves a variety of dependencies including Apache Derby, Apache Commons Math, jFreeChart, and the psychometrics library.

Building
========

You will first need version 1.3 of the psychometrics library, which can be obtained from

https://github.com/meyerjp3/psychometrics/tree/5f2bf9b0509883ec938eef40b28fe25263d466d6

Download that to its own separate directory and build it there with `mvn clean install`.

Now to import it into the jmetrik directory. Assuming the psychometrics project was downloaded
to `/projects/psychometrics`, run the following command from within the jmetrik directory:

```
mvn install:install-file -Dfile=/psychometrics/target/psychometrics-1.3.jar -DgroupId=com.itemanalysis -DartifactId=psychometrics -Dversion=1.3 -Dpackaging=jar
```

Once that's done, you can build jmetrik itself with `mvn clean install`.
