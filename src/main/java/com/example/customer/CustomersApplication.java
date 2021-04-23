package com.example.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class CustomersApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomersApplication.class, args);

	}

	@Bean
	ApplicationRunner runner(DatabaseClient dbc,
							  CustomerRepository repository) {
		return event -> {
			var ddl  = dbc.sql("create table customer(id serial primary key, name varchar (255) not null)").fetch().rowsUpdated();
			var writes = Flux.just("Kallavan","Evankhell","Mazino","Rak","Saket")
					.map(name -> new Customer(null, name))
					.flatMap(repository::save);
			var all = repository.findAll();

			ddl.thenMany(writes).thenMany(all).subscribe(System.out::println);
				};
		}
	}

@RequiredArgsConstructor
@RestController
class CustomerRestController {

	private final CustomerRepository customerRepository;

	@GetMapping("/customers")
	Flux<Customer> get() {
		return this.customerRepository.findAll();
	}
}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

	@Id
	private Integer id;
	private String name;
}


