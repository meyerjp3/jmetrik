/*
 * Copyright (c) 2012 Patrick Meyer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.itemanalysis.jmetrik.workspace;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

public class JmetrikPassword {
    
    public JmetrikPassword(){
        
    }
    
    public String encodePassword(String pw){
        byte[] encodedBytes = Base64.encodeBase64(StringUtils.getBytesUtf8(pw));
        String encodedContent = StringUtils.newStringUtf8(encodedBytes);
        return encodedContent;
    }
    
    public String decodePassword(String pw){
        byte[] decode = Base64.decodeBase64(pw);
        String decodeString = StringUtils.newStringUtf8(decode);
        return decodeString;
    }
    
}
