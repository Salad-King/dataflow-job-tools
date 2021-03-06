package launcher;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dataflow.Dataflow;
import com.google.api.services.dataflow.Dataflow.Projects.Templates.Create;
import com.google.api.services.dataflow.model.CreateJobFromTemplateRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

public class Launcher {
	public static void launchJob(String GCSpath,String jobName,Map<String,String> params) throws IOException, GeneralSecurityException {
		// Authentication is provided by gcloud tool when running locally
		// and by built-in service accounts when running on GAE, GCE or GKE.
		GoogleCredential credential = GoogleCredential.getApplicationDefault();

		// The createScopedRequired method returns true when running on GAE or a local developer
		// machine. In that case, the desired scopes must be passed in manually. When the code is
		// running in GCE, GKE or a Managed VM, the scopes are pulled from the GCE metadata server.
		// See https://developers.google.com/identity/protocols/application-default-credentials for more information.
		if (credential.createScopedRequired()) {
			credential = credential.createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
		}

		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		Dataflow dataflowService = new Dataflow.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName("Google Cloud Platform Sample")
				.build();

		String projectId = "kishan-last-quota";

		CreateJobFromTemplateRequest job = new CreateJobFromTemplateRequest();
		job.setGcsPath(GCSpath)
		.setJobName(jobName);
		if(params!=null)
			job.setParameters(params);

		Create req = dataflowService.projects().templates().create(projectId, job);
		System.out.println("Launching Template:"+GCSpath);
		req.execute();
	}
}