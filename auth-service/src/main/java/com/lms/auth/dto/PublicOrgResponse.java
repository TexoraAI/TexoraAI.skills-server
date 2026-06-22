//package com.lms.auth.dto;
//
//import java.util.UUID;
//
//public class PublicOrgResponse {
//
//    private UUID id;
//    private String name;
//
//    public PublicOrgResponse() {}
//
//    public PublicOrgResponse(UUID id, String name) {
//        this.id = id;
//        this.name = name;
//    }
//
//    public UUID getId() { return id; }
//    public void setId(UUID id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//}

package com.lms.auth.dto;

import java.io.Serializable;
import java.util.UUID;

// OPTIMIZATION: Implemented Serializable for Redis cache compatibility.
public class PublicOrgResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;

    public PublicOrgResponse() {}

    public PublicOrgResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}