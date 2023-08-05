package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    TrainService trainService;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        List<Ticket> bookedTickets = train.getBookedTickets();

        SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto(train.getTrainId(),bookTicketEntryDto.getFromStation(),bookTicketEntryDto.getToStation());

        int availableTickets = trainService.calculateAvailableSeats(seatAvailabilityEntryDto);

        if(availableTickets - bookedTickets.size() < bookTicketEntryDto.getNoOfSeats()) {
            throw new Exception("Less tickets are available");
        }

        String[] stations = train.getRoute().split(",");
        int fromLoc = 0;
        int toLoc = 0;
        int count = 0;
        for (String station : stations) {
            if (station.equals(String.valueOf(bookTicketEntryDto.getFromStation()))) fromLoc = count;
            if (station.equals(String.valueOf(bookTicketEntryDto.getToStation()))) toLoc = count;
            count++;
            if (fromLoc != 0 && toLoc != 0) break;
        }
        if(!(fromLoc > 0 && fromLoc < toLoc) ) throw new Exception("Invalid stations");

        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTrain(train);
        ticket.setTotalFare(bookTicketEntryDto.getNoOfSeats() * (toLoc-fromLoc) * 300);

        Ticket savedTicket = ticketRepository.save(ticket);

        for(int i=0;i<bookTicketEntryDto.getNoOfSeats();i++) {
            Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getPassengerIds().get(i)).get();
            List<Ticket> oldTicketList = passenger.getBookedTickets();
            oldTicketList.add(savedTicket);
            passenger.setBookedTickets(oldTicketList);
            passengerRepository.save(passenger);
        }

       return savedTicket.getTicketId();

    }
}
