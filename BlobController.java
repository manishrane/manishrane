import reactor.core.publisher.Mono;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BlobController {

    private final BlobServiceClient blobServiceClient;

    public BlobController(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @GetMapping(value = "/blob/{containerName}/{blobName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<byte[]>> streamBlob(@PathVariable String containerName, @PathVariable String blobName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // Stream the blob content using Mono
        Mono<byte[]> blobMono = Mono.fromCallable(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);
            return outputStream.toByteArray();
        });

        // Return the Mono as a response
        return blobMono.map(byteArray -> ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + blobName + "\"")
            .body(byteArray));
    }

    import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/download")
public class FileDownloadController {

    @GetMapping("/large-file")
    public ResponseEntity<StreamingResponseBody> downloadLargeFile(@RequestParam("filename") String filename) throws IOException {
        // Replace with the actual path to your large file
        String filePath = "path/to/your/large-file/" + filename;
        File file = new File(filePath);

        if (!file.exists()) {
            // Handle file not found error
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                // Handle streaming error
                e.printStackTrace();
            }
        };

        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBody);
    }
}

}
