package com.davidtwilliams.dtw;

/**
 * Created by paulwilliams on 29/01/15.
 */
public class ScoreData {
        private int ends;
        private int arrowsperend;
        private int[][] data;

        public ScoreData() {}

        public ScoreData(int ends, int arrowsperend, int[][] data) {
            this.ends = ends;
            this.arrowsperend = arrowsperend;
            this.data = data;
        }

       /* public int getEnds() {
            return ends;
        }

        public int getArrowsPerEnd() {
            return arrowsperend;
        }*/

        public int[][] getData() {
            return data;
        }


}
