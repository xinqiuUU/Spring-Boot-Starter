package com.yc.commons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

//托管...
public class DBHelper {

    private final DbProperties p;

    public DBHelper(DbProperties p) {
        this.p = p;
        try {
            Class.forName(p.getDriver());//加载驱动
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //获取一个Connection
    public Connection getConnection(  ) throws SQLException {
        Connection con= DriverManager.getConnection(  p.getUrl(),
                                                    p.getUsername(),
                                                    p.getPassword()
                                            );
        return con;
    }

    /*
     * 批量操作 事务处理 sqls 多条sql语句 params 多条sql语句的参数
     */
    public int update(List<String> sqls, List<List<Object>> params) {
        int result = 0;
        Connection con = null;
        int order_id  = 0;
        try {
            con=getConnection();//获取联接
            con.setAutoCommit(false);// 静止事务自动提交
            // 循环sql语句
            for (int i = 0; i < sqls.size(); i++) {
                String sql = sqls.get(i);
                List<Object> list = params.get(i);
                PreparedStatement pstmt = null;
                if (i == 0){
                    pstmt = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                }else {
                    pstmt = con.prepareStatement(sql);
                }
                if (i != 0){
                    list.add(0,order_id);
                }
                setParams(pstmt, list.toArray());
                result = pstmt.executeUpdate();
                if (i == 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            order_id = generatedKeys.getInt(1); // 获取生成的主键值
                        } else {
                            throw new SQLException("未返回生成的主键");
                        }
                    }
                }
                if (result <= 0) {
                    // 事务回滚
                    con.rollback();
                    break;
                }
            }
            con.commit();// 正常执行完毕后，进行事务提交
        } catch (SQLException e) {
            result = 0; // 发生异常，结果为0
            try {
                con.rollback();// 事务回滚
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println("update  批处理更新异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                con.setAutoCommit(true);// 还原连接状态 启动事务的自动提交
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public int adminUpdate(List<String> sqls, List<List<Object>> params) {
        int result = 0;
        Connection con = null;
        try {
            con=getConnection();//获取联接
            con.setAutoCommit(false);// 静止事务自动提交
            // 循环sql语句
            for (int i = 0; i < sqls.size(); i++) {
                String sql = sqls.get(i);
                List<Object> list = params.get(i);
                PreparedStatement pstmt = con.prepareStatement(sql);
                setParams(pstmt, list.toArray());
                result = pstmt.executeUpdate();
                if (result < 0) {
                    // 事务回滚
                    con.rollback();
                    break;
                }
            }
            con.commit();// 正常执行完毕后，进行事务提交
        } catch (SQLException e) {
            result = 0; // 发生异常，结果为0
            try {
                con.rollback();// 事务回滚
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println("update  批处理更新异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                con.setAutoCommit(true);// 还原连接状态 启动事务的自动提交
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 封装更新( 增加insert, update更新,  delete删除 )
     * sql:是要执行的 更新语句, 这语句有 n个 ?占位符，及对应的n个参数
     *    Object...动态数组， 这个数组的长度不确定，这种参数只能加在一个方法参数列表的最后
     *    sql:  update emp set ename=? , mgr=? where empno=?
     *    params:     '张三','李四', 1101
     */
    public int doUpdate( String sql,  Object... params    ){
        int result=-1;
        try(
                Connection con=getConnection();//获取联接
                PreparedStatement pstmt=con.prepareStatement(   sql );  //预编译语句对象
        ) {
            setParams(pstmt,params );
            //执行语句
            result = pstmt.executeUpdate();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    private  void setParams(PreparedStatement pstmt,Object... params ) throws SQLException {
        //问题一:  ?对应的参数类型是什么，这个类型是什么，则  setXxx()????
        // =>   将所有的参数类型指定为  Object，  =》  setObject();
        //问题二:  总共有几个参数???params到底有几个.
        //   params是动态数组，  length
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * 基于模板设计模式的查询方法.
     * @param rowMapper: 对一行结果集的处理，返回一个对应的对象
     * @param sql
     * @param params
     * @return
     * @param <T>
     */
    public <T> List<T>  select(RowMapper<T> rowMapper, String sql, Object...params  ) throws SQLException {
        List<T> list=new ArrayList<>();
        //查询步骤的模板
        try(
                Connection con=getConnection();
                PreparedStatement pstmt=con.prepareStatement(sql);) {
            this.setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            int num = 0;
            while (rs.next()) {
                //结果集每一行的处理，由RowMapper接口的实现决定   <=程序员
                T t = rowMapper.mapRow(rs, num);
                num++;
                list.add(t);
            }
        }catch(Exception ex){
            throw ex;
        }
        return list;
    }

    /**  方法名相同，参数不同=>重载方法
     * 查询返回值是一个  List<T>  T代表任意的类的对象.
     *     T类标准javaBean: 属性封装(private), 对外提供 get/set    setXxxx(参数)
     * @param c: 代表  T 类的反射类的对象 (T基因 )
     * @param sql
     * @param params
     * @return
     * @param <T>
     */
    //  List<Dept> list=db.select(   Dept.class, sql    );
    public <T> List<T> select(  Class<T> c,  String sql,    Object...params ) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        List<T> result=new ArrayList<>();
        //1. sql, params =>查询得到数据表数据
        List<Map<String,Object>> list=this.select(   sql,  params);

        //将Map中的值取出  转出一个T对象.

        for(  Map<String,Object> map:list) {
            T t=  c.newInstance();  //调用了这个T类的无参构造方法.
            //2.  将Map<String,Object> 转换成   T对象  .
            //1. 循环map中的所有的键值    entrySet()   Set.iterator()   ->  iterator.hasnext()
            Set<Map.Entry<String,Object>> set=map.entrySet();
            Iterator<Map.Entry<String,Object>> iterator=set.iterator();
            while(  iterator.hasNext()  ){
                Map.Entry<String,Object> entry= iterator.next();
                String key=entry.getKey();
                key=key.substring(0,1).toUpperCase()+key.substring(1).toLowerCase();
                Object value=entry.getValue();
                //System.out.println( key+"   :   "+value );   //
                if (value==null || "".equals(value)){
                    continue;
                }
                //TODO: 取出 c中取应的那个  setXxx, 激活它，设置值(类型转换 ).
                //key=>  setXxx
                String methodName="set"+key;
                Method setMethod= findSetMethod(  methodName,  c  )  ;
                //System.out.println(   "当前属性为:"+ key+",值为:"+value+",对应的方法为:"+ setMethod  );
                //激活此方法, 设置值进去
                // 取出  setMethod中的参数类型，进行判断，看它是哪种
                Class parameterTypeClass=setMethod.getParameterTypes()[0];
                String parameterTypeName=parameterTypeClass.getName();
                if(  "int".equals(parameterTypeName) || "java.lang.Integer".equals(parameterTypeName)){
                    int p=Integer.parseInt( value.toString());
                    setMethod.invoke(t, p);
                }else  if(  "float".equals(parameterTypeName) || "java.lang.Float".equals(parameterTypeName)){
                    float p=Float.parseFloat( value.toString());
                    setMethod.invoke(t, p);
                }else  if(  "double".equals(parameterTypeName) || "java.lang.Double".equals(parameterTypeName)){
                    double p=Double.parseDouble( value.toString());
                    setMethod.invoke(t, p);
                }else  if(  "byte".equals(parameterTypeName) || "java.lang.Byte".equals(parameterTypeName)){
                    byte p=Byte.parseByte( value.toString());
                    setMethod.invoke(t, p);
                }else  if(  "long".equals(parameterTypeName) || "java.lang.Long".equals(parameterTypeName)){
                    long p=Long.parseLong( value.toString());
                    setMethod.invoke(t, p);
                }else  if(  "short".equals(parameterTypeName) || "java.lang.Short".equals(parameterTypeName)){
                    short p=Short.parseShort( value.toString());
                    setMethod.invoke(t, p);
                }else  if(  "boolean".equals(parameterTypeName) || "java.lang.Boolean".equals(parameterTypeName)){
                    boolean p=Boolean.parseBoolean( value.toString());
                    setMethod.invoke(t, p);
                }else {
                    setMethod.invoke(t, value.toString());
                }
            }

            //3. 将T对象存到List中.
            result.add(t);
        }
        return result;
    }

    /**
     * 从c中找出  名字为  methodName的方法
     * @param methodName
     * @param c
     * @return
     * @param <T>
     */
    private <T> Method findSetMethod(String methodName, Class<T> c) {

        Method[] ms=c.getDeclaredMethods();
        for(   Method m:ms){
            if(  methodName.equals( m.getName() )){
                return m;
            }
        }
        return null;
    }



    /**
     * 查询
     * @param sql
     * @param params
     */
    public List<Map<String,Object>> select(String sql, Object...params){
        List<Map<String,Object>> list=new ArrayList<>();
        try(
                Connection con=getConnection();//获取联接
                PreparedStatement pstmt=con.prepareStatement(   sql );  //预编译语句对象
        ) {
            setParams(pstmt,params );
            ResultSet rs=pstmt.executeQuery();
            // jdbc中规范  ResultSet中有关于结果集的一切信息
            ResultSetMetaData rsmd=rs.getMetaData();   //结果集元数据   => 有多少个列  ,每个列叫什么名字
            int columnCount= rsmd.getColumnCount();  //列的数量
            //循环结果集的行.
            while(  rs.next() ){
                Map<String,Object> map=new HashMap<String,Object>();  //一行就是一个map
                //那么到底有几个列啊????
                for( int i=0;i<columnCount;i++){
                    //数据类型
                    //System.out.print( rs.getObject(   i+1 )+"\t");   // TODO:  这个数据不能这样处理...
                    map.put(   rsmd.getColumnName(i+1),   rs.getObject(   i+1)   );   //存每一列...
                }
                list.add( map );  //将这个map存到 list
                // System.out.println();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return list;
    }

}
