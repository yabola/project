import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;
import spark.jobserver.api.JobEnvironment;
import spark.jobserver.japi.JSparkJob;

import java.util.Arrays;
import java.util.Iterator;


public class TestServer implements JSparkJob<String> {

    @Override
    public String run(JavaSparkContext sc, JobEnvironment runtime, Config data) {
        JavaRDD<String> lines=sc.textFile("hdfs://dsdc81:9000/demo.data");
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String,String>(){
            @Override
            public Iterator<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" ")).iterator();
            }
        });
        JavaPairRDD<String,Integer> pairs =words.mapToPair(new PairFunction<String,String,Integer>(){
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String,Integer>(word,1);
            }

        });
        JavaPairRDD<String,Integer> wordCounts =pairs.reduceByKey(new Function2<Integer,Integer,Integer>(){
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1+v2;
            }
        });
        wordCounts.foreach(new VoidFunction<Tuple2<String,Integer>>(){
            @Override
            public void call(Tuple2<String, Integer> wordcount) throws Exception {
                System.out.println(wordcount._1+" appeared "+wordcount._2+"times");
            }
        });
        return "Hi!";
    }
    @Override
    public Config verify(JavaSparkContext sc, JobEnvironment runtime, Config config) {
        return ConfigFactory.empty();
    }
}