package util.project.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
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

    private static final AmazonS3 S3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();

    public static void main(String[] args) {
        SpringApplication.run(AwsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        printArgOrder();
        downloadFile(args);
    }

    private static void printArgOrder() {
        System.out.printf(
                "Arg1: %s, Arg2: %s, Arg3: %s\n",
                "Bucket name",
                "File name",
                "Last modified date"
        );
    }

    private static void downloadFile(String... args) {
        final String bucketName = args[0];
        final String fileName = args[1];
        final Date lastModifiedDate = Date.from(Instant.parse(args[2] + "T00:00:00Z"));

        getLatestFile(bucketName, fileName, lastModifiedDate)
                .ifPresentOrElse(
                        summary -> {
                            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, summary.getKey(), summary.getVersionId());
                            S3.getObject(getObjectRequest, new File("downloaded/" + fileName));
                        },
                        () -> System.out.println("Could not download the file!")
                );

    }

    private static Optional<S3VersionSummary> getLatestFile(String bucketName, String fileName, Date lastModifiedDate) {
        List<S3VersionSummary> summaries = S3.listVersions(bucketName, null)
                .getVersionSummaries()
                .stream()
                .filter(summary -> fileName.equals(summary.getKey()))
                .filter(summary -> summary.getLastModified().before(lastModifiedDate))
                .collect(Collectors.toUnmodifiableList());

        return summaries
                .stream()
                .reduce((summary1, summary2) -> summary1.getLastModified().after(summary2.getLastModified())
                        ? summary1
                        : summary2
                );
    }
}
