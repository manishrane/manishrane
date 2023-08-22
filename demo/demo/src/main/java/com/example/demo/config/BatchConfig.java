import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // ... Other beans and methods ...

    @Bean
    public Job loginJob() {
        return jobBuilderFactory.get("loginJob")
                .start(loginStep())
                .build();
    }

    @Bean
    @JobScope
    public Step loginStep(@Value("#{jobParameters['loginUrl']}") String loginUrl) {
        return stepBuilderFactory.get("loginStep")
                .tasklet(loginTasklet(loginUrl))
                .build();
    }

    @Bean
    @JobScope
    public Tasklet loginTasklet(String loginUrl) {
        return (contribution, chunkContext) -> {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(loginUrl, String.class);
            
            String token = extractTokenFromResponse(response.getBody());
            // Do something with the token
            
            return RepeatStatus.FINISHED;
        };
    }

    private String extractTokenFromResponse(String response) {
        // Implement the logic to extract the token from the response
        // Return the extracted token
    }

    // ... Other beans and methods ...
}
