package com.uangel.command;

import com.uangel.model.SimType;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author dajin kim
 */
@Getter
@ToString
public class CommandInfo {
    private static Options opts;

    // Service
    private final String service;
    private final String scenarioFile;
    private final SimType type;
    // Proto
    private final String protoFile;
    private final String protoPkg;

    // RMQ
    private final String rmqLocal;
    private final String rmqHost;
    private final String rmqUser;
    private final int rmqPort;
    private final String rmqPass;

    private final String rmqTarget;
    private final String rmqTargetHost;
    private final String rmqTargetUser;
    private final int rmqTargetPort;
    private final String rmqTargetPass;

    private final int rmqThreadSize;
    private final int rmqQueueSize;

    // Performance
    private final int threadSize;

    private final int rate;
    private final int ratePeriod;
    private final int duration;
    private final int limit;
    private final double rateIncrease;
    private final double rateMax;
    private final int maxCall;


    // Command Line UDP Port


    public CommandInfo(CommandLine cmd) {
        // Service
        this.service = cmd.getOptionValue("s", "service");
        this.scenarioFile = cmd.getOptionValue("sf");
        String mode = cmd.getOptionValue("t", "json");
        this.type = SimType.getTypeEnum(mode);
        // Proto
        this.protoFile = cmd.getOptionValue("pf");
        this.protoPkg = cmd.getOptionValue("pkg");

        // RMQ
        this.rmqLocal = cmd.getOptionValue("rl");
        this.rmqHost = cmd.getOptionValue("rh");
        this.rmqUser = cmd.getOptionValue("ru");
        this.rmqPort = Integer.parseInt(cmd.getOptionValue("rp", "5672"));
        this.rmqPass = cmd.getOptionValue("rpw");

        this.rmqTarget = cmd.getOptionValue("rt");
        this.rmqTargetHost = cmd.getOptionValue("rth");
        this.rmqTargetUser = cmd.getOptionValue("rtu");
        this.rmqTargetPort = Integer.parseInt(cmd.getOptionValue("rtp", "5672"));
        this.rmqTargetPass = cmd.getOptionValue("rtpw");

        this.rmqThreadSize = Integer.parseInt(cmd.getOptionValue("rts", "10"));
        this.rmqQueueSize = Integer.parseInt(cmd.getOptionValue("rqs", "1000"));

        // Performance
        this.threadSize = Integer.parseInt(cmd.getOptionValue("ts", "10"));

        // todo calculate
        this.rate = Integer.parseInt(cmd.getOptionValue("r", "10"));
        this.ratePeriod = Integer.parseInt(cmd.getOptionValue("rp", "1000"));
        this.duration = Integer.parseInt(cmd.getOptionValue("d", "0"));
        this.limit = Integer.parseInt(cmd.getOptionValue("l", Integer.toString(3 * rate * (duration == 0 ? 1 : duration))));
        this.rateIncrease = Double.parseDouble(cmd.getOptionValue("rate_increase", "0"));
        this.rateMax = Double.parseDouble(cmd.getOptionValue("rate_max", "100"));
        this.maxCall = Integer.parseInt(cmd.getOptionValue("m", "0"));

    }

    public static Options createOptions() {
        if(opts != null) return opts;
        opts = new Options();

        // Service
        opts.addOption(new Option("h", "display help text"));
        opts.addOption(Option.builder("s").argName("service").hasArg().desc("Service name field").build());

        // Scenario File options
        opts.addOption(Option.builder("sf").argName("file").hasArg().desc("The XML scenario file").build());

        // Simulator Type
        opts.addOption(Option.builder("t").argName("simulator_type").hasArg().desc("Simulator Mode [json/proto]").build());
        opts.addOption(Option.builder("pf").argName("proto_file").hasArg().desc("Proto jar file (required when in proto mode)").build());
        opts.addOption(Option.builder("pkg").argName("proto_package").hasArg().desc("Proto base package name").build());

        // RMQ
        opts.addOption(Option.builder("rl").argName("rmq_local").hasArg().desc("RMQ Local Queue name").build());
        opts.addOption(Option.builder("rh").argName("rmq_host").hasArg().desc("RMQ Local host").build());
        opts.addOption(Option.builder("ru").argName("rmq_user").hasArg().desc("RMQ Local user").build());
        opts.addOption(Option.builder("rp").argName("rmq_port").hasArg().desc("RMQ Local port").build());
        opts.addOption(Option.builder("rpw").argName("rmq_pass").hasArg().desc("RMQ Local password").build());

        opts.addOption(Option.builder("rt").argName("rmq_target").hasArg().desc("RMQ Target Queue name").build());
        opts.addOption(Option.builder("rth").argName("rmq_target_host").hasArg().desc("RMQ Target host").build());
        opts.addOption(Option.builder("rtu").argName("rmq_target_user").hasArg().desc("RMQ Target user").build());
        opts.addOption(Option.builder("rtp").argName("rmq_target_port").hasArg().desc("RMQ Target port").build());
        opts.addOption(Option.builder("rtpw").argName("rmq_target_pass").hasArg().desc("RMQ Target password").build());

        opts.addOption(Option.builder("rts").argName("rmq_thread_size").hasArg().desc("RMQ Thread size").build());
        opts.addOption(Option.builder("rqs").argName("rmq_queue_size").hasArg().desc("RMQ Queue size").build());


        opts.addOption(Option.builder("ts").argName("thread_size").hasArg().desc("Thread size. (default : (limit / 20) + 10)").build());

        // Call behavior options
        opts.addOption(Option.builder("d").argName("duration").hasArg().desc("Controls the length of calls. More precisely, this controls the duration of 'pause' instructions in the scenario, if they do not have a 'milliseconds'").build());

        // Call rate options
        opts.addOption(Option.builder("r").argName("rate").hasArg().desc("The number of new calls to be created per second (default 10)").build());
        opts.addOption(Option.builder("rp").argName("rate_period").hasArg().desc("Specify the rate period for the call rate. unit is milliseconds (default 10)").build());
        opts.addOption(Option.builder("l").argName("limit").hasArg().desc("et the maximum number of simultaneous calls").build());
        opts.addOption(Option.builder("rate_increase").argName("rate_increase").hasArg().desc("If rate should ramp up periodically, specify the number of calls/second it should increase by").build());
        //opts.addOption(Option.builder("rate_increase_period").argName("rate_increase_period").hasArg().desc("If rate should ramp up periodically, specify the number of seconds between each step up").build());
        opts.addOption(Option.builder("rate_max").argName("rate_max").hasArg().desc("If rate should ramp up periodically, specify the maximum number of calls/second").build());
        opts.addOption(Option.builder("m").argName("max").hasArg().desc("Stop the test and exit when 'calls' calls are processed").build());
        //opts.addOption(Option.builder("users").argName("users").hasArg().desc("Instead of starting calls at a fixed rate, begin 'users' calls at startup, and keep the number of calls constant.").build());

        return opts;
    }
}
