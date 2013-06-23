#! /usr/bin/env ruby
class Collector
    def initialize(input_file)
        @input = File.readlines(input_file)
    end

    def work
        result = {}
        @input.each { |line| result[$1] = $2 if line =~ /\((\d+)\)\s=\s(\d+)/ }
        puts result.values
    end
end

ARGV << "result.txt" if ARGV.empty?
collector = Collector.new(ARGV[0])
collector.work