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
    public ResponseEntity<byte[]> streamBlob(@PathVariable String containerName, @PathVariable String blobName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        // Stream the blob content
        InputStream blobStream = blobClient.openQueryParquetContent();
        // You can use any method to read from 'blobStream' and convert it to a byte array
        
        // Return the byte array as a response
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + blobName + "\"")
            .body(byteArray);
    }
}
