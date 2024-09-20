package com.yc.commons;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ResultSet中的一行如何处理成对象
 */
public interface RowMapper<T> {
    /**
     * 由最终用户决定如何处理 ResultSet中的第rowNum行.
     * @param rs
     * @param rowNum
     * @return   这一行对应的对象.
     */
    T  mapRow(ResultSet rs, int rowNum) throws SQLException;
}
