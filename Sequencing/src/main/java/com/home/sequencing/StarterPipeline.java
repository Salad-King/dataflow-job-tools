package com.home.sequencing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;


class Job1 implements Runnable,Serializable{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(StarterPipeline.class);

	@Override
	public void run() {
		DataflowPipelineOptions options = PipelineOptionsFactory.as(DataflowPipelineOptions.class);
		options.setRunner(DataflowRunner.class);
		options.setStagingLocation("gs://bucket-ot/Trash/staging");
		options.setProject("kishan-last-quota");
		options.setJobName("job1");
		options.setTempLocation("gs://bucket-ot/Templates/firstTemp");		
		Pipeline p = Pipeline.create(options);

		PCollection<TableRow> data = p.apply(Create.of("data1,pipeline1","data2,pipeline1","data3,pipeline1","data4,pipeline1","data5,pipeline1","data6,pipeline1"))
				.apply(ParDo.of(new DoFn<String,TableRow>(){
					private static final long serialVersionUID = 1L;

					@ProcessElement
					public void processElement(ProcessContext c){
						String entry = c.element();
						TableRow ob = new TableRow();
						String[] line = entry.split("[,]");
						ob.set("f1", line[0]);
						ob.set("f2", line[1]);
						LOG.info(ob.toString());
						c.output(ob);
					}
				}));

		List<TableFieldSchema> fields = new ArrayList<>();
		fields.add(new TableFieldSchema().setName("f1").setType("STRING"));
		fields.add(new TableFieldSchema().setName("f2").setType("STRING"));

		TableSchema schema = new TableSchema().setFields(fields);

		data.apply(BigQueryIO.writeTableRows().
				to("tameems_dataset.trash")
				.withSchema(schema)
				.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
				.withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED));

		p.run();
	}
}

class Job2 implements Runnable,Serializable{
	private static final long serialVersionUID = -4151287153256195875L;
	private static final Logger LOG = LoggerFactory.getLogger(Job2.class);

	public void run(){
		DataflowPipelineOptions options = PipelineOptionsFactory.as(DataflowPipelineOptions.class);
		options.setRunner(DataflowRunner.class);
		options.setStagingLocation("gs://bucket-ot/Trash/staging");
		options.setProject("kishan-last-quota");
		options.setTempLocation("gs://bucket-ot/Trash/staging");
		Pipeline p = Pipeline.create(options);

		PCollection<TableRow> data = p.apply(BigQueryIO.read().from("tameems_dataset.trash"))
				.apply(ParDo.of(new DoFn<TableRow,TableRow>(){
					private static final long serialVersionUID = 1L;

					@ProcessElement
					public void processElement(ProcessContext c){
						TableRow ob = c.element().clone();
						ob.remove("f2");
						ob.set("f2", "Pipeline2");
						LOG.info(ob.toString());
						c.output(ob);
					}
				}));

		List<TableFieldSchema> fields = new ArrayList<>();
		fields.add(new TableFieldSchema().setName("f1").setType("STRING"));
		fields.add(new TableFieldSchema().setName("f2").setType("STRING"));

		TableSchema schema = new TableSchema().setFields(fields);

		data.apply(BigQueryIO.writeTableRows().
				to("tameems_dataset.trashOut")
				.withSchema(schema)
				.withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
				.withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED));

		p.run();
	}

}

public class StarterPipeline{
	public static void main(String[] args) throws InterruptedException{
		Thread firstJob = new Thread(new Job1());
		Thread secondJob = new Thread(new Job2());
		firstJob.start();
		firstJob.join();
		secondJob.start();
		secondJob.join();
	}
}