require "sinatra"
require 'json'
require 'faker'
require 'pry'

get '/data/jsonp' do
  puts "*** Request received"
  binding.pry if pry?
  sleep 5 if sleep?

  content_type 'application/json'
  msg_count = (request.params['mode'] == 'last') ? 10 : 5
  wrap_into_callback compose_chat_json(msg_count)
end

def pry?
  ENV['PRY']
end

def sleep?
  ENV['SLEEP']
end

def wrap_into_callback(json)
  "callback_fn(#{json})"
end

def compose_chat_json(count)
  { "records" => records_array(count) }.to_json
end

def records_array(count)
  (1..count).collect { |i| create_chat_record }
end

def create_chat_record
  {
    'msg' => generate_message,
    'from' => Faker::Name.name,
    'time' => 'Sat Dec 15 22:12:10 UTC 2012'
  }
end

def generate_message
  if rand < 0.3
    Faker::Lorem.paragraph
  else
    Faker::Lorem.sentence
  end
end






