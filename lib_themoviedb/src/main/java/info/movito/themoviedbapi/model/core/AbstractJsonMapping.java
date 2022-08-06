package info.movito.themoviedbapi.model.core;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;


/**
 * @author Holger Brandl
 */
public abstract class AbstractJsonMapping implements Serializable {


    /**
     * Handle unknown properties and print a message
     */
    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append("Unknown property: '").append(key);
        sb.append("' value: '").append(value).append("'");

    }

}
