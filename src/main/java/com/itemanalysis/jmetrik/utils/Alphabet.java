package com.itemanalysis.jmetrik.utils;

/**
 * Copyright 2012 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public enum Alphabet {

    A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z;

    /**
     * Returns the order of the letter in teh alphabet. The order is zero based.
     *
     * @param targ
     * @return
     */
    public static int getNum(String targ) {
        return valueOf(targ.toUpperCase()).ordinal();
    }

    public static int getNum(char targ) {
        String s = Character.toString(targ);
        return getNum(s);
    }

    public static String getLetter(int pos, boolean lowerCase){
        if(lowerCase){
            switch (pos){
                case 1: return "a";
                case 2: return "b";
                case 3: return "c";
                case 4: return "d";
                case 5: return "e";
                case 6: return "f";
                case 7: return "g";
                case 8: return "h";
                case 9: return "i";
                case 10: return "j";
                case 11: return "k";
                case 12: return "l";
                case 13: return "m";
                case 14: return "n";
                case 15: return "o";
                case 16: return "p";
                case 17: return "q";
                case 18: return "r";
                case 19: return "s";
                case 20: return "t";
                case 21: return "u";
                case 22: return "v";
                case 23: return "w";
                case 24: return "x";
                case 25: return "y";
                case 26: return "z";
            }
        }else{
            switch (pos){
                case 1: return "A";
                case 2: return "B";
                case 3: return "C";
                case 4: return "D";
                case 5: return "E";
                case 6: return "F";
                case 7: return "G";
                case 8: return "H";
                case 9: return "I";
                case 10: return "J";
                case 11: return "K";
                case 12: return "L";
                case 13: return "M";
                case 14: return "N";
                case 15: return "O";
                case 16: return "P";
                case 17: return "Q";
                case 18: return "R";
                case 19: return "S";
                case 20: return "T";
                case 21: return "U";
                case 22: return "V";
                case 23: return "W";
                case 24: return "X";
                case 25: return "Y";
                case 26: return "Z";
            }
        }
        return null;
    }

}


