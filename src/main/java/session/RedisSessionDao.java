package session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.util.SerializationUtils;
import util.JedisUtil;


import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/*
* generateSessionId(session) 生成Serializable序列化的sessionId

   assignSessionId(session,sessionId) 绑定session与相应的Id值

session.getId 返回的是SessionId值，同理也是Serializable的类型

SerializationUtils.serialize (session) 通过序列化将session转成相应的byte[]数组

SerializationUtils.deserialize(byte[]) 通过反序列化将字节转成Object类型，可通过强转获取 Session对象.*/

public class RedisSessionDao extends AbstractSessionDAO {

    @Resource
    private JedisUtil jedisUtil;

    private final  String   SHIRO_SESSIONID_PREFIX = "shiro_session:";

    private byte[] getKey(String sessionId){
        return (SHIRO_SESSIONID_PREFIX+sessionId).getBytes();
    }

    private void saveSession(Session session){
        if(session!=null  && session.getId()!=null){
            byte[] key = getKey(session.getId().toString());
            byte[] value = SerializationUtils.serialize(session);
            jedisUtil.set(key,value);
            jedisUtil.expire(key,600);
        }
    }
    @Override
    protected Serializable doCreate(Session session) {

        Serializable sessionId = generateSessionId(session);
        assignSessionId(session,sessionId);
        saveSession(session);
        //System.out.println(sessionId.toString()+" "+session.getId());
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        System.out.println("read session");
        if(sessionId == null){
            return  null;
        }
        byte[] key = getKey(sessionId.toString());
        byte[] value = jedisUtil.get(key);
        Session session = (Session) SerializationUtils.deserialize(value);
        return session;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        saveSession(session);
    }

    @Override
    public void delete(Session session) {
        if(session!=null && session.getId()!=null){
            byte[] key = getKey(session.getId().toString());
            jedisUtil.del(key);
        }

    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<byte[]> keys = jedisUtil.keys(SHIRO_SESSIONID_PREFIX);
        if(keys == null ||keys.isEmpty()){
            return null;
        }
        Set<Session> sessionSet = new HashSet<>();
        for(byte[] key: keys){
            sessionSet.add((Session) SerializationUtils.deserialize(jedisUtil.get(key)));
        }
        return sessionSet;
    }
}
