package com.example;

import org.springframework.boot.SpringApplication;

public class TestRoomReservationApplication {

	public static void main(String[] args) {
		SpringApplication.from(RoomReservationApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
