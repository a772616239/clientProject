/**
 * created by tool DAOGenerate
 */
package daoMaster;


import java.util.List;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import db.core.BaseDAO;
import model.player.entity.playerEntity;

/**
 * created by tool
 */

public interface playerDAO extends BaseDAO<playerEntity> {

    @Insert("INSERT INTO player(idx,shortid,userid,name,avatar,level,experience,vip,vipexperience,gold,diamond,createtime,logintime,logouttime,updatetime,monthcardexpiretime,coupon,playerdata,dailyrewardrecord,oncerewardreward,sex) VALUES (#{idx},#{shortid},#{userid},#{name},#{avatar},#{level},#{experience},#{vip},#{vipexperience},#{gold},#{diamond},#{createtime},#{logintime},#{logouttime},#{updatetime},#{monthcardexpiretime},#{coupon},#{playerdata},#{dailyrewardrecord},#{oncerewardreward},#{sex});")
    public void insert(playerEntity entity);

    @Delete("delete from player where idx = #{idx}")
    public void delete(String idx);

    @Update("update player set idx=#{idx},shortid=#{shortid},userid=#{userid},name=#{name},avatar=#{avatar},level=#{level},experience=#{experience},vip=#{vip},vipexperience=#{vipexperience},gold=#{gold},diamond=#{diamond},createtime=#{createtime},logintime=#{logintime},logouttime=#{logouttime},updatetime=#{updatetime},monthcardexpiretime=#{monthcardexpiretime},coupon=#{coupon},playerdata=#{playerdata},dailyrewardrecord=#{dailyrewardrecord},oncerewardreward=#{oncerewardreward},sex=#{sex} where idx = #{idx}")
    public void update(playerEntity entity);

    @Select("SELECT * FROM player")
    public List<playerEntity> listAll();


    @Insert("INSERT INTO player(idx,shortid,userid,name,avatar,level,experience,vip,vipexperience,gold,diamond,createtime,logintime,logouttime,updatetime,monthcardexpiretime,coupon,playerdata,dailyrewardrecord,oncerewardreward) VALUES (0,0,0,0,0,0,0,0,0,0,0,null,null,null,null,null,0,0,0,0);")
    public void test();

    @Select("SELECT MAX(shortid) FROM player")
    Integer getMaxShortId();
}