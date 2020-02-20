import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Arrays;


public class BaseballElimination {
    private String[] teams;
    private int[] wins;
    private int[] losses;
    private int[] remaining;
    private int[][] games;
    private int teamCount;
    private FordFulkerson ff;
    private FlowNetwork network;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        In input = new In(filename);
        teamCount = input.readInt();

        teams = new String[teamCount];
        wins = new int[teamCount];
        losses = new int[teamCount];
        remaining = new int[teamCount];
        games = new int[teamCount][teamCount];

        for (int i = 0; i < teamCount; i++) {
            teams[i] = input.readString();
            wins[i] = input.readInt();
            losses[i] = input.readInt();
            remaining[i] = input.readInt();
            for (int j = 0; j < teamCount; j++) {
                games[i][j] = input.readInt();
            }
        }
    }

    private int teamIndex(String team) {
        int index = -1;
        for (int i = 0; i < teamCount; i++) {
            if (teams[i].equals(team)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new IllegalArgumentException("Invalid team");
        }

        return index;
    }

    // number of teams
    public int numberOfTeams() {
        return teamCount;
    }

    // all teams
    public Iterable<String> teams() {
        return Arrays.asList(teams);
    }

    // number of wins for given team
    public int wins(String team) {
        if (team == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        return wins[teamIndex(team)];
    }

    // number of losses for given team
    public int losses(String team) {
        if (team == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        return losses[teamIndex(team)];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        if (team == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        return remaining[teamIndex(team)];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        return games[teamIndex(team1)][teamIndex(team2)];
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        if (team == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        int highestPossible = wins(team) + remaining(team);
        for (int i = 0; i < teams.length; i++) {
            if (!teams[i].equals(team)) {
                if (wins[i] > highestPossible) {
                    return true;
                }
            }
        }

        int otherTeams = teamCount - 1;
        int checkIndex = teamIndex(team);
        network = new FlowNetwork(otherTeams + 2 + ((otherTeams - 1) * otherTeams) / 2);

        int gameVertex = 1;
        int teamVertex = 1;
        for (int i = 0; i < teamCount; i++) {
            if (i != checkIndex) {
                for (int j = i + 1; j < teamCount; j++) {
                    if (j != checkIndex) {
                        network.addEdge(new FlowEdge(0, gameVertex, games[i][j]));
                        if (i < checkIndex) {
                            network.addEdge(new FlowEdge(gameVertex, ((otherTeams - 1) * otherTeams) / 2 + i + 1, Double.POSITIVE_INFINITY));
                        } else {
                            network.addEdge(new FlowEdge(gameVertex, ((otherTeams - 1) * otherTeams) / 2 + i, Double.POSITIVE_INFINITY));
                        }
                        if (j < checkIndex) {
                            network.addEdge(new FlowEdge(gameVertex, ((otherTeams - 1) * otherTeams) / 2 + j + 1, Double.POSITIVE_INFINITY));
                        } else {
                            network.addEdge(new FlowEdge(gameVertex, ((otherTeams - 1) * otherTeams) / 2 + j, Double.POSITIVE_INFINITY));

                        }
                        gameVertex++;
                    }
                }
                network.addEdge(new FlowEdge(((otherTeams - 1) * otherTeams) / 2 + teamVertex, network.V() - 1, wins[checkIndex] + remaining[checkIndex] - wins[i]));
                teamVertex++;
            }
        }

        ff = new FordFulkerson(network, 0, network.V() - 1);
        for (FlowEdge e : network.adj(0)) {
            if (e.capacity() != e.flow()) {
                return true;
            }
        }

        return false;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        if (team == null) {
            throw new IllegalArgumentException("Argument is null");
        }

        if (!isEliminated(team)) {
            return null;
        }

        ArrayList<String> subset = new ArrayList<>();

        int highestPossible = wins(team) + remaining(team);
        for (int i = 0; i < teams.length; i++) {
            if (!teams[i].equals(team)) {
                if (wins[i] > highestPossible) {
                    subset.add(teams[i]);
                    return subset;
                }
            }
        }

        int checkIndex = teamIndex(team);
        int otherTeams = teamCount - 1;

        for (int i = 0; i < teamCount; i++) {
            if (i < checkIndex) {
                if (ff.inCut(i + 1 + ((otherTeams - 1) * otherTeams / 2))) {
                    subset.add(teams[i]);
                }
            } else if (i > checkIndex) {
                if (ff.inCut(i + ((otherTeams - 1) * otherTeams / 2))) {
                    subset.add(teams[i]);
                }
            }
        }

        return subset;
    }

    // testing
    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
