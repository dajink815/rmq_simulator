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
    private  String scenarioFile;
    private  String fieldKeyword;
    private  SimType type;
    private int longSession;
    private boolean testMode;

    // Proto
    private  String protoFile;
    private  String protoPkg;

    // RMQ
    private  String rmqLocal;
    private  String rmqHost;
    private  String rmqUser;
    private  int rmqPort;
    private  String rmqPass;

    private  String rmqTarget;
    private  String rmqTargetHost;
    private  String rmqTargetUser;
    private  int rmqTargetPort;
    private  String rmqTargetPass;

    private  int rmqQueueSize;

    // Performance
    private  int threadSize;
    private  int rate;
    private  int ratePeriod;
    private  int duration;
    private  int limit;
    private  int maxCall;

    // Command Line UDP Port

    // User Command File Path
    private String userCmdFilePath;

    // RTP
    private int minRtpPort;
    private int maxRtpPort;
    private int mediaTimestampGap;
    private int mediaSendGap;
    private int rtpBundle;

    public CommandInfo(CommandLine cmd) {
        loadServiceConfig(cmd);
        loadProtoConfig(cmd);
        loadRmqConfig(cmd);
        loadPerfConfig(cmd);
        ladaRtpConfig(cmd);
    }

    private void ladaRtpConfig(CommandLine cmd) {
        this.minRtpPort = Integer.parseInt(cmd.getOptionValue("min_rtp_port", "0"));
        this.maxRtpPort = Integer.parseInt(cmd.getOptionValue("max_rtp_port", "0"));
        this.mediaTimestampGap = Integer.parseInt(cmd.getOptionValue("media_timestamp_gap", "160"));
        this.mediaSendGap = Integer.parseInt(cmd.getOptionValue("media_send_gap", "20"));
        this.rtpBundle = Integer.parseInt(cmd.getOptionValue("rtp_bundle", "1"));
    }

    public void loadServiceConfig(CommandLine cmd) {
        this.scenarioFile = cmd.getOptionValue("sf");
        this.fieldKeyword = cmd.getOptionValue("k", "callId");
        String mode = cmd.getOptionValue("t", "proto");
        this.type = SimType.getTypeEnum(mode);
        this.longSession = Integer.parseInt(cmd.getOptionValue("long_session", "300"));

        String testModeStr = cmd.getOptionValue("test_mode", "false");
        this.testMode = "true".equalsIgnoreCase(testModeStr);
        this.userCmdFilePath = cmd.getOptionValue("user_cmd");
    }

    public void loadProtoConfig(CommandLine cmd) {
        this.protoFile = cmd.getOptionValue("pf", "");
        this.protoPkg = cmd.getOptionValue("pkg", "");
        if (protoPkg != null && !protoPkg.endsWith(".")) {
            protoPkg += ".";
        }
    }

    public void loadRmqConfig(CommandLine cmd) {
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

        this.rmqQueueSize = Integer.parseInt(cmd.getOptionValue("rqs", "1000"));
    }

    public void loadPerfConfig(CommandLine cmd) {
        try {
            this.threadSize = Integer.parseInt(cmd.getOptionValue("ts", "10"));
            this.rate = Integer.parseInt(cmd.getOptionValue("r", "10"));
            this.ratePeriod = Integer.parseInt(cmd.getOptionValue("rpd", "1000"));
            this.duration = Integer.parseInt(cmd.getOptionValue("d", "0"));
            this.limit = Integer.parseInt(cmd.getOptionValue("l", Integer.toString(5 * rate * (duration == 0 ? 1 : duration))));
            this.maxCall = Integer.parseInt(cmd.getOptionValue("m", "1"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static Options createOptions() {
        if(opts != null) return opts;
        opts = new Options();

        // Service
        opts.addOption(new Option("h", "display help text"));
        opts.addOption(Option.builder("sf").argName("file").hasArg().desc("The XML scenario file").build());
        opts.addOption(Option.builder("k").argName("field_keyword").hasArg().desc("Field keyword").build());
        opts.addOption(Option.builder("long_session").argName("long_session").hasArg().desc("Long Session timer (unit:sec, default:300s)").build());
        opts.addOption(Option.builder("test_mode").argName("test_mode").hasArg().desc("Test Mode").build());
        opts.addOption(Option.builder("user_cmd").argName("user_cmd_file").hasArg().desc("user exec command file path").build());

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

        opts.addOption(Option.builder("rqs").argName("rmq_queue_size").hasArg().desc("RMQ Queue size").build());
        opts.addOption(Option.builder("ts").argName("thread_size").hasArg().desc("Thread size. (default : (limit / 20) + 10)").build());

        // Call behavior options
        opts.addOption(Option.builder("d").argName("duration").hasArg().desc("Controls the length of calls. More precisely, this controls the duration of 'pause' instructions in the scenario, if they do not have a 'milliseconds'").build());

        // Call rate options
        opts.addOption(Option.builder("r").argName("rate").hasArg().desc("The number of new calls to be created per second (default 10)").build());
        opts.addOption(Option.builder("rpd").argName("rate_period").hasArg().desc("Specify the rate period for the call rate. unit is milliseconds (default 10)").build());
        opts.addOption(Option.builder("l").argName("limit").hasArg().desc("et the maximum number of simultaneous calls").build());
        opts.addOption(Option.builder("m").argName("max").hasArg().desc("Stop the test and exit when 'calls' calls are processed").build());

        // RTP behaviour options
        opts.addOption(Option.builder("min_rtp_port").argName("min_media_port").hasArg().desc("Minimum port number for RTP socket range").build());
        opts.addOption(Option.builder("max_rtp_port").argName("max_media_port").hasArg().desc("Maximum port number for RTP socket range").build());
        opts.addOption(Option.builder("media_timestamp_gap").argName("media_timestamp_gap").hasArg().desc("RTP Timestamp will increase by [media_timestamp_gap]. default : 160").build());
        opts.addOption(Option.builder("media_send_gap").argName("media_send_gap").hasArg().desc("RTP send every [media_send_gap]ms. default : 20").build());
        opts.addOption(Option.builder("rtp_bundle").argName("rtp_bundle").hasArg().desc("If rtp_bundle 1, send 1 rtp by [media_send_gap]ms. If rtp_bundle 50, send 50 rtp by 50 * [media_send_gap] ms").build());

        return opts;
    }
}
