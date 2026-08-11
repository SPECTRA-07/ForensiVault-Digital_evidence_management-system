package com.dems.config;

import com.dems.storage.LocalStorageServiceImpl;
import com.dems.storage.S3StorageServiceImpl;
import com.dems.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StorageConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StorageProperties.class, LocalStorageServiceImpl.class, S3StorageServiceImpl.class);

    @Test
    void whenProviderNotSet_thenLocalStorageServiceIsLoaded() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(StorageService.class);
            assertThat(context).hasBean("localStorageServiceImpl");
            assertThat(context).doesNotHaveBean(S3StorageServiceImpl.class);
        });
    }

    @Test
    void whenProviderSetToLocal_thenLocalStorageServiceIsLoaded() {
        contextRunner.withPropertyValues("app.storage.provider=local").run(context -> {
            assertThat(context).hasSingleBean(StorageService.class);
            assertThat(context).hasBean("localStorageServiceImpl");
            assertThat(context).doesNotHaveBean(S3StorageServiceImpl.class);
        });
    }

    @Test
    void whenProviderSetToS3_thenS3StorageServiceIsLoaded() {
        contextRunner.withPropertyValues(
                "app.storage.provider=s3",
                "app.storage.bucket=test-bucket",
                "app.storage.region=us-east-1"
        ).run(context -> {
            assertThat(context).hasSingleBean(StorageService.class);
            assertThat(context).hasBean("s3StorageServiceImpl");
            assertThat(context).doesNotHaveBean(LocalStorageServiceImpl.class);
        });
    }
}
