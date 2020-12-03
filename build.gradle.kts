import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
	kotlin("jvm") version "1.4.10"
	kotlin("plugin.spring") version "1.4.10"
}

group = "com.kostas"
version = ""
java.sourceCompatibility = JavaVersion.VERSION_12

repositories {
	mavenCentral()
	maven {
		url = uri("http://packages.confluent.io/maven/")
	}
}

object Versions {
	const val kafkaClients = "2.6.0"
	const val springKafka = "2.6.2"
	const val avro = "1.10.0"
	const val avroSerializer = "6.0.0"
	const val avroSerde = "5.5.1"
	const val arrow = "0.11.0"
	const val mockk = "1.10.0"
	const val assertj = "3.16.1"
	const val jacksonAvro = "2.11.3"
	const val kotlinLogging = "1.8.3"
	const val logbackEncoder = "6.4"
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.arrow-kt:arrow-core:${Versions.arrow}")
	implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
	implementation("net.logstash.logback:logstash-logback-encoder:${Versions.logbackEncoder}")

	implementation("org.apache.kafka:kafka-clients:${Versions.kafkaClients}")
	implementation("org.apache.kafka:kafka-streams:${Versions.kafkaClients}")
	implementation("org.springframework.kafka:spring-kafka:${Versions.springKafka}")

	implementation("org.apache.avro:avro:${Versions.avro}")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-avro:${Versions.jacksonAvro}")
	implementation("io.confluent:kafka-avro-serializer:${Versions.avroSerializer}")
	implementation("io.confluent:kafka-streams-avro-serde:${Versions.avroSerde}")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("io.mockk:mockk:${Versions.mockk}")
	testImplementation("org.assertj:assertj-core:${Versions.assertj}")
	testImplementation("org.springframework.kafka:spring-kafka-test:${Versions.springKafka}") {
		exclude(group = "org.apache.kafka", module = "kafka_2.11")
	}
	testImplementation("org.apache.kafka:kafka-clients:${Versions.kafkaClients}:test")
	testImplementation("org.apache.kafka:kafka-streams-test-utils:${Versions.kafkaClients}")
	testImplementation("org.apache.kafka:kafka-streams-test-utils:${Versions.kafkaClients}:test")
	testImplementation("org.apache.kafka:kafka_2.13:${Versions.kafkaClients}")
	testImplementation("org.apache.kafka:kafka_2.13:${Versions.kafkaClients}:test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "12"
	}
}
