package com.pat.reactiveclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;

@Slf4j
@SpringBootApplication
public class ReactiveClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveClientApplication.class, args);
	}

	@Bean
	WebClient webClient() {
		return WebClient.create("http://localhost:8080/");
	}

	@Bean
	CommandLineRunner commandLineRunner(WebClient webClient) {
		return args -> webClient.get()
                .uri("movies")
                .exchange()
                .flatMapMany(cr -> cr.bodyToFlux(Movie.class))
				.filter(movie -> movie.getTitle().toLowerCase().contains("The Fluxinator".toLowerCase()))
                .subscribe(
                		movie -> webClient.get().uri("/movies/"+movie.getId()+"/events")
							.exchange()
							.flatMapMany(clientResponse -> clientResponse.bodyToFlux(MovieEvent.class))
								.map(movieEvent -> "movieEvent=" + movieEvent )
							.subscribe(log::info)
				);
	}
}

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
class Movie {
	String id, title, genre;
}

@Data
@ToString
@NoArgsConstructor
class MovieEvent {
	private Movie movie;
	private Date when;
	private String user;

	MovieEvent(final Movie movie, final Date when, final String user) {
		this.movie = movie;
		this.when = when;
		this.user = user;
	}
}