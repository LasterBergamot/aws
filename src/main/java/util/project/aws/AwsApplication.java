package util.project.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import java.io.File;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AwsApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AwsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.printf(
                "Arg1: %s, Arg2: %s, Arg3: %s\n",
                "Bucket name",
                "File name",
                "Last modified date"
        );

        String bucketName = args[0];
        String fileName = args[1];
        String lastModifiedDate = args[2] + "T00:00:00Z";

        System.out.printf("Bucket name: %s\n", bucketName);
        System.out.printf("File name: %s\n", fileName);
        System.out.printf("Last modified date: %s\n", lastModifiedDate);

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

        Date date = Date.from(Instant.parse(lastModifiedDate));

        VersionListing versionListing = s3.listVersions(bucketName, null);
        List<S3VersionSummary> summaries = versionListing.getVersionSummaries()
                .stream()
                .filter(summary -> fileName.equals(summary.getKey()))
                .filter(summary -> summary.getLastModified().before(date))
                .collect(Collectors.toUnmodifiableList());

        Optional<S3VersionSummary> latest = summaries
                .stream()
                .reduce(
                        (s3VersionSummary, s3VersionSummary2) ->
                                s3VersionSummary.getLastModified().after(s3VersionSummary2.getLastModified())
                                        ? s3VersionSummary
                                        : s3VersionSummary2
                );

        S3VersionSummary s3VersionSummary = latest.get();
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, s3VersionSummary.getKey(), s3VersionSummary.getVersionId());
        ObjectMetadata latestObject = s3.getObject(getObjectRequest, new File("downloaded/" + fileName));

        ObjectListing objectListing = s3.listObjects(bucketName);

        System.out.println();
    }
}
