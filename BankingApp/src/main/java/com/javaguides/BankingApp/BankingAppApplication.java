package com.javaguides.BankingApp;
import com.javaguides.BankingApp.services.systemDefaultsServices;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BankingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingAppApplication.class, args);

	}

	@Bean
	public CommandLineRunner setup(systemDefaultsServices systemDefaultsServices) {
		return args -> {
			systemDefaultsServices.setTransactionFee();
		};
	}


}
