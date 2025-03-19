package org.example;

public class Main {
    public static void main(String[] args) {
        //ConcurrencyControl.handleTournamentRegistrations(4, 1);
        //ConcurrencyControl.submitMatchResultV2Test(3,8);
        ConcurrencyControl.testTournamentRegistrationsLastSpot(9, 1);

        //ConcurrencyControl.performanceTestOptimistic();
        //ConcurrencyControl.performanceTestPessimistic();
        try{
            //ConcurrencyControl.performanceTestPessimisticTracker(50);
            //ConcurrencyControl.performanceTestOptimisticTracker(50);
        }
        catch (Exception e) {
            e.printStackTrace();

        }
    }
}
