package fa.training.blog.exception.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
    // This configuration file has @EnableJpaAuditing to use @CreatedDate and @LastModifiedDate
    // on createDate and modifyDate fields in entity Post and Comment.
}
