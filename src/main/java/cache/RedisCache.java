package cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import util.JedisUtil;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Set;

@Component
public class RedisCache<k,v> implements Cache<k,v> {

    @Resource
    private JedisUtil jedisUtil;
    private  final  String SHIRO_CACHE_PREFIX="cache";

    byte[] getKey(k k){
        if(k instanceof String){
            return (SHIRO_CACHE_PREFIX+k).getBytes();
        }
        return SerializationUtils.serialize(k);
    }
    @Override
    public v get(k k) throws CacheException {

        System.out.println("from cache");
        byte[] value = jedisUtil.get(getKey(k));
        if(value!=null){
            return (v) SerializationUtils.deserialize(value);
        }
        return null;
    }

    @Override
    public v put(k k, v v) throws CacheException {
        if(k!=null && v!=null){
            byte[] key = getKey(k);
            byte[] value = SerializationUtils.serialize(v);
            jedisUtil.set(key,value);
            jedisUtil.expire(key,600);
        }
        return v;
    }

    @Override
    public v remove(k k) throws CacheException {
        v v = null;
        if(k!=null){
            byte[] key = getKey(k);
            byte[] value = jedisUtil.get(key);
            if(value!=null){
                jedisUtil.del(key);
                v =  (v) SerializationUtils.deserialize(value);
            }
           ;
        }
        return v;
    }

    @Override
    public void clear() throws CacheException {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<k> keys() {
        return null;
    }

    @Override
    public Collection<v> values() {
        return null;
    }
}
