redis.call('DEL', KEYS[2])
if #ARGV == 0 then
    return redis.call('DEL', KEYS[1])
end
redis.call('ZADD', KEYS[2], unpack(ARGV))
redis.call('RENAME', KEYS[2], KEYS[1])
return 1
