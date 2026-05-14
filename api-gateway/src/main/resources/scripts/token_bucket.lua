--[[
  Token Bucket Rate Limiter — Atomic Lua Script
  
  KEYS[1] = Redis key  (e.g. rl:user:john@example.com:auth-login)
  
  ARGV[1] = replenishRate   (tokens per second)
  ARGV[2] = burstCapacity   (max tokens / bucket size)
  ARGV[3] = now             (current epoch seconds)
  ARGV[4] = requested       (tokens to consume, typically 1)
  
  Returns: { allowed (1/0), remaining, resetEpoch }
]]

local key           = KEYS[1]
local replenishRate = tonumber(ARGV[1])
local burstCapacity = tonumber(ARGV[2])
local now           = tonumber(ARGV[3])
local requested     = tonumber(ARGV[4])

-- TTL: bucket expires after it would be fully refilled
-- minimum 60s so short-burst keys don't evict too fast
local ttl = math.max(math.ceil(burstCapacity / math.max(replenishRate, 1)), 60)

local data = redis.call("HMGET", key, "tokens", "last_refill")

local tokens      = tonumber(data[1])
local last_refill = tonumber(data[2])

if tokens == nil then
    -- First request: start full
    tokens      = burstCapacity
    last_refill = now
end

-- Refill tokens based on elapsed time
if replenishRate > 0 then
    local elapsed = math.max(0, now - last_refill)
    local new_tokens = elapsed * replenishRate
    tokens = math.min(burstCapacity, tokens + new_tokens)
end

last_refill = now

local allowed   = 0
local remaining = tokens

if tokens >= requested then
    tokens    = tokens - requested
    remaining = tokens
    allowed   = 1
end

-- Persist updated bucket state
redis.call("HMSET", key, "tokens", tokens, "last_refill", last_refill)
redis.call("EXPIRE", key, ttl)

-- resetEpoch = when bucket will be full again (approximate)
local tokens_needed = burstCapacity - tokens
local reset_in = 0
if replenishRate > 0 then
    reset_in = math.ceil(tokens_needed / replenishRate)
end
local reset_epoch = now + reset_in

return { allowed, remaining, reset_epoch }