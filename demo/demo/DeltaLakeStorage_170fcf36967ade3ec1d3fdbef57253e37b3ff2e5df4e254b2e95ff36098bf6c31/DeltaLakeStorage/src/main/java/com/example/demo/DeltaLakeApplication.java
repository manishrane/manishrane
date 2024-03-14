package com.example.demo;

import io.delta.exceptions.DeltaConcurrentModificationException;
import io.delta.standalone.*;
import io.delta.standalone.actions.Action;
import io.delta.standalone.actions.AddFile;
import io.delta.standalone.expressions.*;
import io.delta.standalone.types.StructType;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.filter.UnboundRecordFilter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;

import java.io.IOException;
import java.sql.Date;
import java.util.*;

import static org.apache.parquet.filter.ColumnPredicates.equalTo;
import static org.apache.parquet.filter.ColumnRecordFilter.column;


@Slf4j
public class DeltaLakeApplication {


    public static void getAllEmployees(DeltaLog log) {
        Snapshot latestSnapshot = log.update();
        List<Employee> employeeList = new ArrayList<Employee>();
        latestSnapshot.open().forEachRemaining(data -> {
            employeeList.add(Employee.map(data));
        });
        System.out.println("data ::: " + employeeList);
    }

    public static Employee getEmployee(DeltaLog log, Integer id) throws IOException {
        Snapshot latestSnapshot = log.update();
        StructType schema = latestSnapshot.getMetadata().getSchema();
        DeltaScan scan = latestSnapshot.scan(
                new And(
                        new And(
                                new GreaterThan(schema.column("birthDate"), Literal.of(Date.valueOf("2022-01-01"))
                                ), new LessThan(schema.column("birthDate"), Literal.of(Date.valueOf("2022-03-01"))
                        )),
                        new EqualTo(schema.column("id"), Literal.of(104))
                )
        );

        System.out.println(scan.getResidualPredicate());
        scan.getFiles().forEachRemaining(file -> {
            try {
                Path path = new Path(Constants.ABSS_FILE_PATH + file.getPath());
                UnboundRecordFilter unboundRecordFilter = column("id", equalTo(id));
                ParquetReader<GenericRecord> reade1 = new AvroParquetReader<>(path, unboundRecordFilter);
                GenericRecord nextRecord = reade1.read();
//                System.out.println("Schema ::: " + nextRecord.getSchema());
                if (nextRecord != null) {
                    System.out.println("Employee:::: " + nextRecord);
                }

            } catch (IOException e) {
                System.out.println("exception:::: " + e);
                e.printStackTrace();
            }
        });

        return null;
    }


    public static void createEmployee(DeltaLog log) throws IOException {

        String fileName = "part-" + UUID.randomUUID().toString() + ".c000.snappy.parquet";
        Path filePath = new Path("abfss://test@ubsgen2storageuk.dfs.core.windows.net/Employees2/birthDate=2022-02-01/", fileName);
        System.out.println("Path ::: " + filePath);

        final ParquetWriter<GenericData.Record> parquetWriter =
                AvroParquetWriter.<GenericData.Record>builder(filePath).withSchema(getSchema())
                        .withPageSize(AvroParquetWriter.DEFAULT_PAGE_SIZE)
                        .withDictionaryPageSize(AvroParquetWriter.DEFAULT_BLOCK_SIZE)
                        .withMaxPaddingSize(AvroParquetWriter.MAX_PADDING_SIZE_DEFAULT)
                        .build();
        final GenericData.Record fooRecord = new GenericData.Record(getSchema());
        fooRecord.put("id", 124);
        fooRecord.put("firstName", "Suram");
        fooRecord.put("middleName", "Yeadd");
        fooRecord.put("lastName", "Singh");
        fooRecord.put("gender", "Male");
        fooRecord.put("ssn", "666");
        fooRecord.put("salary", 1245);
        fooRecord.put("birthDate", "2017-08-09");

        parquetWriter.write(fooRecord);


        System.out.println("Size ::: " + parquetWriter.getDataSize());

        ParquetReader<GenericRecord> reade1 = new AvroParquetReader<>(filePath);
        GenericRecord nextRecord = reade1.read();
        System.out.println("Schema ::: " + nextRecord.getSchema());
        if (nextRecord != null) {
            System.out.println("Employee:::: " + nextRecord);
        }

        OptimisticTransaction txn = log.startTransaction();
        Map<String, String> partitionValue = new HashMap<String, String>();
        partitionValue.put("birthDate", "2022-02-09");

        AddFile addNewFiles = new AddFile(
                filePath.toString(),
                partitionValue,
                parquetWriter.getDataSize(),
                System.currentTimeMillis(),
                true, // isDataChange
                nextRecord.toString(), // stats
                null  // tags
        );

        List<Action> totalCommitFiles = new ArrayList<>();
        totalCommitFiles.add(addNewFiles);

        try {
            txn.commit(totalCommitFiles, new Operation(Operation.Name.WRITE), "Commit");
        } catch (DeltaConcurrentModificationException e) {
            // handle exception here
        }

    }

    public static Schema getSchema() {

        final Schema schema =
                Schema.createRecord("spark_schema", null, "org.apache.parquet", false);

        schema.setFields(Arrays.asList(
                new Schema.Field("id", Schema.create(Schema.Type.INT), null, null),
                new Schema.Field("firstName", Schema.create(Schema.Type.STRING), null, null),
                new Schema.Field("middleName", Schema.create(Schema.Type.STRING), null, null),
                new Schema.Field("lastName", Schema.create(Schema.Type.STRING), null, null),
                new Schema.Field("gender", Schema.create(Schema.Type.STRING), null, null),
                new Schema.Field("ssn", Schema.create(Schema.Type.STRING), null, null),
                new Schema.Field("salary", Schema.create(Schema.Type.INT), null, null),
                new Schema.Field("birthDate", Schema.create(Schema.Type.STRING), null, null)
        ));

        return schema;
    }

    public static void main(String[] args) {
        Configuration conf = DeltaLakeConfiguration.getConfiguration();
        try {
            DeltaLog log = DeltaLog.forTable(conf, new Path(Constants.TABLE_NAME));
            getEmployee(log, 104);
//            createEmployee(log);
//            getAllEmployees(log);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
