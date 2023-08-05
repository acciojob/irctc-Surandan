package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();

        StringBuilder stringBuilder = new StringBuilder();
        List<Station> stationList = trainEntryDto.getStationRoute();
        for(int i=0;i<stationList.size()-1;i++) {
            stringBuilder.append(stationList.get(i)).append(",");
        }
        stringBuilder.append(stationList.get(stationList.size()-1));

        train.setRoute(stringBuilder.toString());
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        Train savedTrain = trainRepository.save(train);

        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();

        String[] stations = train.getRoute().split(",");
        int [] indexes = getIndex(stations,seatAvailabilityEntryDto.getFromStation(),seatAvailabilityEntryDto.getToStation());
        int fromLocVal = indexes[0];
        int toLocVal = indexes[1];


        Integer totalSeats = train.getNoOfSeats();
        Integer occupiedSeats = 0;

        List<Ticket> ticketList = train.getBookedTickets();
        for(Ticket ticket : ticketList) {
            int [] tindexes = getIndex(stations,seatAvailabilityEntryDto.getFromStation(),seatAvailabilityEntryDto.getToStation());
            int cfromLocVal = tindexes[0];
            int ctoLocVal = tindexes[1];

            if((cfromLocVal >= fromLocVal && cfromLocVal <= toLocVal) || (ctoLocVal >= fromLocVal && cfromLocVal <= toLocVal)) {
                occupiedSeats +=(ticket.getPassengersList().size());
            }
        }

        return totalSeats - occupiedSeats;
    }

    public int[] getIndex(String[] stations,Station fromLocation,Station toLocation) {
        int fromLocVal = 0;
        int toLocVal = 0;

        for(int i=0;i<stations.length;i++) {
            if(stations[i].equals(String.valueOf(fromLocation))) fromLocVal = i;
            if(stations[i].equals(String.valueOf(toLocation))) toLocVal = i;
        }

        return new int[] {fromLocVal,toLocVal};
    }
    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        String[] stations = train.getRoute().split(",");
        boolean stationFound = false;
        for (String station1 : stations) {
            if(station1.equals(String.valueOf(station))) {
                stationFound = true;
                break;
            }
        }
        if (!stationFound) throw new Exception("Train is not passing from this station");

        Integer count = 0;
        List<Ticket> ticketList = train.getBookedTickets();

        for (Ticket ticket : ticketList) {
            if(String.valueOf(ticket.getToStation()).equals(String.valueOf(station))) count += (ticket.getPassengersList().size());
        }

        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Integer oldestAge = 0;
        Train train = trainRepository.findById(trainId).get();
        if (train.getBookedTickets().isEmpty()) return oldestAge;

        for(Ticket ticket : train.getBookedTickets()) {
            for (Passenger passenger : ticket.getPassengersList()) {
                if (passenger.getAge() > oldestAge) oldestAge = passenger.getAge();
            }
        }

        return oldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> trainIds = new ArrayList<>();
        List<Train> trainList = trainRepository.findAll();

        int indexOfStation = 0;
        for(Train train : trainList) {
            String[] stations = train.getRoute().split(",");
            for(int i=0;i<stations.length;i++) {
                if(stations[i].equals(String.valueOf(station))) {
                    indexOfStation = i;
                    break;
                }
            }
            LocalTime trainArrivalTime = train.getDepartureTime().plusHours(indexOfStation);
            if(trainArrivalTime.isBefore(startTime) || trainArrivalTime.isAfter(endTime)) continue;
            else trainIds.add(train.getTrainId());
        }


        return trainIds;
    }

}
