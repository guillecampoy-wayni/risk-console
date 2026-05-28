package com.example.riskconsole.api;

import com.example.riskconsole.application.SnapshotService;
import com.example.riskconsole.domain.Snapshot;
import com.example.riskconsole.domain.repository.SnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotControllerTest {

    @Test
    void createsSnapshotOnPostRequest() {
        var service = new FakeSnapshotService();
        var client = WebTestClient.bindToController(new SnapshotController(service)).build();

        client.post()
                .uri("/api/risk/snapshots")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.createdAt").isNotEmpty();
    }

    @Test
    void listsAllSnapshotsOnGetRequest() {
        var service = new FakeSnapshotService();
        var client = WebTestClient.bindToController(new SnapshotController(service)).build();

        client.get()
                .uri("/api/risk/snapshots")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Snapshot.class)
                .hasSize(2);
    }

    @Test
    void delegatesCreateToServiceAndReturnsSnapshot() {
        var service = new FakeSnapshotService();
        var controller = new SnapshotController(service);

        controller.create().as(StepVerifier::create)
                .assertNext(snapshot -> {
                    assertThat(snapshot.id()).isEqualTo("snap-1");
                    assertThat(snapshot.createdAt()).isNotNull();
                })
                .verifyComplete();
    }

    private static class FakeSnapshotService extends SnapshotService {
        FakeSnapshotService() {
            super((SnapshotRepository) null);
        }

        @Override
        public Mono<Snapshot> takeSnapshot() {
            return Mono.just(new Snapshot("snap-1", Instant.parse("2026-05-28T12:00:00Z")));
        }

        @Override
        public Flux<Snapshot> listSnapshots() {
            return Flux.just(
                    new Snapshot("snap-1", Instant.parse("2026-05-28T12:00:00Z")),
                    new Snapshot("snap-2", Instant.parse("2026-05-28T13:00:00Z"))
            );
        }
    }
}
