class Convertor
    def initialize(input_file, output_file)
        @output = File.open(output_file, "w")
        @input  = File.readlines(input_file)
    end

    def convert
        result = []
        # puts @input
        @input.each do |line|
            new_line = line.split('//')[0].split('@')[0].strip.gsub(/\s+/, '')
            result << new_line if new_line.length > 0
        end
        # puts result
        @output.puts result
    end
end

ARGV << 'ram_data.txt' << 'ram_data.hex' if ARGV.empty?
Convertor.new(ARGV[0], ARGV[1]).convert