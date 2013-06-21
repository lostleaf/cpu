#! /usr/bin/env ruby
#coding: utf-8
require "pry"
class Assembler
    def initialize(input, output)
        @INST   = %w[add sub mul lwrr swrr addi subi muli lw sw li j jr bge]
        @REG    = %w[zero at v0 v1 a0 a1 a2 a3 t0 t1 t2 t3 t4 t5 t6 t7 s0 s1
                     s2 s3 s4 s5 s6 s7 t8 t9 k0 k1 gp sp fp ra]
        # binding.pry
        @input, @output = input.read, output
    end

    def convert_reg_name(reg_name)
        return reg_name unless reg_name.include? '$' #not a illegal register name
        return @REG.find_index(reg_name.strip.delete('$')) || reg_name
    end

    def step1
        @label_map, @insts = {}, []

        @input.each_line do |line|
            line = line.split('#')[0].strip
            next if line.length == 0

            #collect labels
            if line.end_with? ':'
                @label_map[line.chop] = @insts.size
                next
            end

            #split oprands with instruction
            inst_str, op_str = line.split(/\s+/,2)
            ops = op_str.gsub(/\s+/,'').split(',')

            @insts << [@INST.find_index(inst_str), ops, line]
        end
    end

    def step2
        @insts.each do |inst_code, ops, inst_ori|
            opsc = ops.collect { |op| convert_reg_name op}
            case inst_code
            when 0..4       #add..swrr
                @output.puts "%04b_%05b_%05b_%05b_#{'0'*13}" % [inst_code, *opsc]
            when 5..9       #addi..sw
                @output.puts "%04b_%05b_%05b_%018b" % [inst_code, *opsc]
                # @output.puts "#%04b_%05d_%05d_%018d" % [inst_code, *opsc]
            when 10         #li
                @output.puts "%04b_%05b_%023b" % [inst_code, *opsc]
            when 11         #j
                @output.puts "%04b_%028b" % [inst_code, @label_map[opsc[0]]]
            when 12         #jr
                @output.puts "%04b_%05b_#{'0'*23}" % [inst_code, opsc[0]]
            when 13         #bge
                @output.puts "%04b_%05b_%010b_%013b" % [inst_code, *opsc[0..1], @label_map[opsc[2]]]
            end
            # @output.puts "\# #{inst_ori}"
        end
    end

    def translate
        step1
        step2
    end
end

ARGV << "code.asm" << "code.bin" if ARGV.empty?

ass = Assembler.new(File.open(ARGV[0], 'r'), File.open(ARGV[1], 'w'))
ass.translate
