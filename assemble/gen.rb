require "matrix"
class DataGenerator

    def initialize(output_file)
        @output = File.open(output_file, "w")
    end

    def my_rand
        (rand * 9).to_i
    end

    def format_matrix(a, str)
        x = a.to_a.inject([]) { |arr, row| arr << (row.map { |x| str % x }); arr }
        x
    end

    def generate_matrix(m, n, l)
        a = Matrix.build(m, n) {my_rand}
        b = Matrix.build(n, l) {my_rand}
        c = a*b
        @output.puts format_matrix(a, "%08x"), format_matrix(b, "%08x")
        puts format_matrix(c, "%d")
    end
end

# ARGV << "ram_data.hex" if ARGV.empty?
g = DataGenerator.new(ARGV[0])
g.generate_matrix(ARGV[1].to_i, ARGV[2].to_i, ARGV[3].to_i)