package org.example;

import com.mysql.cj.protocol.Resultset;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ConcurrencyControl {
    private static final String URL = "jdbc:mysql://207.154.236.26:3306/esport";
    private static final String USER = "devtester";
    private static final String PASSWORD = "testuser";


// 1. Implement Optimistic Concurrency Control for Tournament Updates
    public static boolean changeStartDateTournament (int tournamentId, String newStartDate) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);

            // Read the version of the tournament
            String selectSQL = "SELECT start_date, version FROM tournaments WHERE tournament_id = ?";
            int currentVersion;

            try(PreparedStatement selectStmt = conn.prepareStatement(selectSQL)){
                selectStmt.setInt(1, tournamentId);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Tournament not found.");
                    return false;
                }
                currentVersion = rs.getInt("version");

            }

            String updateSQL = "UPDATE tournaments SET start_date = ?, version = version + 1 WHERE tournament_id = ? AND version = ?";
            try(PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                updateStmt.setString(1, newStartDate);
                updateStmt.setInt(2, tournamentId);
                updateStmt.setInt(3, currentVersion);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    System.out.println("Optimistic locking failed. The tournament was modified by another transaction. Please try again.");
                    conn.rollback();
                    throw new Exception("Optimistic locking failed.");
                } else {
                    conn.commit();
                    System.out.println("Tournament start date changed successfully.");

                    return true;
                }
            }


        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }
// 2. Implement Pessimistic Concurrency Control for Match Updates
    public static void updateMatchResult(int match_id, int winner_id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)){
             conn.setAutoCommit(false);
             String lockSQL = "SELECT * FROM matches WHERE match_id = ? FOR UPDATE";

             try(PreparedStatement lockStmt = conn.prepareStatement(lockSQL)){
                 lockStmt.setInt(1, match_id);
                 lockStmt.executeQuery();
             }

             String updateSQL = "UPDATE matches SET winner_id = ? WHERE match_id = ?";
             try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)){
                 updateStmt.setInt(1, winner_id);
                 updateStmt.setInt(2, match_id);
                 int rowsAffected = updateStmt.executeUpdate();

                 if (rowsAffected == 0){
                     System.out.println("Match result update failed.");
                     conn.rollback();
                 } else {
                     conn.commit();
                     System.out.println("Match result updated successfully.");
                 }
             }


        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

// 3. Handle Transactions for Tournament Registrations
    public static void handleTournamentRegistrations (int playerId, int tournamentId) {
        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)){
            try {
                conn.setAutoCommit(false);
                PreparedStatement lockstmt = conn.prepareStatement("SELECT * FROM players WHERE player_id = ? FOR UPDATE");
                lockstmt.setInt(1, playerId);
                lockstmt.execute();

                CallableStatement stmt = conn.prepareCall("{CALL joinTournament(?, ?)}");
                stmt.setInt(1, playerId);
                stmt.setInt(2, tournamentId);
                stmt.execute();
                PreparedStatement updateRanking = conn.prepareStatement("UPDATE players SET ranking = ranking + 10 WHERE player_id = ?");
                updateRanking.setInt(1, playerId);
                updateRanking.executeUpdate();


                conn.commit();
                System.out.println("Player " + playerId + " joined tournament " + tournamentId + " successfully.");

            } catch (SQLException e) {
                System.err.println("Database error when committing changes: " + e.getMessage());
                conn.rollback();
            }
        }
        catch (SQLException e) {
            System.err.println("Database error when creating a connection: " + e.getMessage());
        }

    }
// 4️. Implement a Stored Procedure for Safe Ranking Updates
    public static void submitMatchResult (int match_id, int winner_id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try{
                conn.setAutoCommit(false);

                PreparedStatement lockStmtMatches = conn.prepareStatement("SELECT * FROM matches WHERE match_id = ? FOR UPDATE");
                lockStmtMatches.setInt(1, match_id);
                ResultSet rs  = lockStmtMatches.executeQuery();
                if (!rs.next()) {
                    System.out.println("Match ID not found: " + match_id);
                    conn.rollback();
                    return;
                }

                int player1_id = rs.getInt("player1_id");
                int player2_id = rs.getInt("player2_id");

                PreparedStatement lockStmtPlayers = conn.prepareStatement("SELECT * FROM players WHERE player_id = ? OR player_id = ? FOR UPDATE");
                lockStmtPlayers.setInt(1, player1_id);
                lockStmtPlayers.setInt(2, player2_id);
                lockStmtPlayers.executeQuery();

                CallableStatement stmt = conn.prepareCall("{CALL submitMatchResult(?, ?)}");
                stmt.setInt(1, match_id);
                stmt.setInt(2, winner_id);
                stmt.execute();
                conn.commit();
                System.out.println("Match result submitted successfully.");



            }
            catch (SQLException e){
                System.out.println("Something went wrong when committing changes" + e.getMessage());
                conn.rollback();

                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            System.err.println("Database error when creating a connection: " + e.getMessage());
        }
    }

    public static void submitMatchResultV2 (int match_id, int winner_id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try{
                CallableStatement stmt = conn.prepareCall("{CALL submitMatchResult2(?, ?)}");
                stmt.setInt(1, match_id);
                stmt.setInt(2, winner_id);
                stmt.execute();
                System.out.println("Match result submitted successfully.");
            }
            catch (SQLException e){
                System.out.println("Something went wrong when committing changes " + e.getMessage());
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            System.err.println("Database error when creating a connection: " + e.getMessage());
        }
    }

    public static void submitMatchResultV2Test (int match_id, int winner_id) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for(int i = 0; i < 2; i++){
            executor.submit(() -> {
                submitMatchResultV2(match_id, winner_id);
            });
        }
        executor.shutdown();
    }

    public static void testTournamentRegistrationsLastSpot(int playerId, int tournamentId){
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for(int i = playerId; i < playerId + 2; i++){
            int finalI = i;
            executor.submit(() -> {
                // insert different delay for the 2 threads
                try {
                    Thread.sleep(finalI * 500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                    CallableStatement stmt = conn.prepareCall("{CALL joinTournament(?, ?)}");
                    stmt.setInt(1, finalI);
                    stmt.setInt(2, tournamentId);
                    stmt.execute();
                } catch (SQLException e) {
                    System.err.println("Database message: " + e.getMessage());
                }
            });
        }
        executor.shutdown();
    }

    public static void performanceTestOptimistic (){
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                boolean success = false;
                long startTime = System.currentTimeMillis();

                while (!success) {
                //    success = changeStartDateTournament(1, "2021-12-01");
                }
                long endTime = System.currentTimeMillis();

                System.out.println("Time taken: " + (endTime - startTime) + "ms");

        });
        }
        executor.shutdown();

    }
    public static void performanceTestPessimistic (){
        ExecutorService executor = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {

            executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                submitMatchResult(3, 1);

                long endTime = System.currentTimeMillis();

                System.out.println("Time taken: " + (endTime - startTime) + "ms");

            });
        }
        executor.shutdown();

    }
        public static void performanceTestPessimisticTracker(int threads) throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            AtomicInteger attempts = new AtomicInteger(0);
            AtomicInteger failures = new AtomicInteger(0);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    attempts.incrementAndGet(); // Track attempt
                    long startTime = System.currentTimeMillis();

                    try {
                        // Simulated method that might fail
                        submitMatchResult(3, 1);
                    } catch (Exception e) {
                        failures.incrementAndGet(); // Track failure
                    }

                    long endTime = System.currentTimeMillis();
                    System.out.println("Time taken: " + (endTime - startTime) + "ms");
                });
            }

            executor.shutdown(); // Prevent new tasks from being submitted

            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) { // Wait for all tasks to complete
                    System.out.println("Some tasks did not finish in time.");
                }
            } catch (InterruptedException e) {
                System.err.println("Executor interrupted: " + e.getMessage());
            }

            // Print final results after all tasks are completed
            System.out.println("Total attempts: " + attempts.get());
            System.out.println("Total failures: " + failures.get());
        }


    public static void performanceTestOptimisticTracker(int threads) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        AtomicInteger attempts = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                attempts.incrementAndGet(); // Track attempt
                long startTime = System.currentTimeMillis();

                try {
                    // Simulated method that might fail
                         changeStartDateTournament(1, "2021-12-01");
                } catch (Exception e) {
                    failures.incrementAndGet(); // Track failure
                }

                long endTime = System.currentTimeMillis();
                System.out.println("Time taken: " + (endTime - startTime) + "ms");
            });
        }
        executor.shutdown(); // Prevent new tasks from being submitted

        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) { // Wait for all tasks to complete
                System.out.println("Some tasks did not finish in time.");
            }
        } catch (InterruptedException e) {
            System.err.println("Executor interrupted: " + e.getMessage());
        }

        // Print final results after all tasks are completed
        System.out.println("Total attempts: " + attempts.get());
        System.out.println("Total failures: " + failures.get());
    }
}
