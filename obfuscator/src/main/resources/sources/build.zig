const std = @import("std");

const platforms: []const std.Target.Query = &.{
    .{ .cpu_arch = .x86_64, .os_tag = .windows },
    .{ .cpu_arch = .aarch64, .os_tag = .windows },

    .{ .cpu_arch = .x86_64, .os_tag = .linux },
    .{ .cpu_arch = .aarch64, .os_tag = .linux },

    .{ .cpu_arch = .x86_64, .os_tag = .macos },
    .{ .cpu_arch = .aarch64, .os_tag = .macos },
};

fn getOutputName(b: *std.Build, target: std.Target) []const u8 {
    const arch = switch (target.cpu.arch) {
        .x86 => "x86",
        .x86_64 => "x64",
        .aarch64 => "arm64",
        else => @panic("unsupported arch"),
    };

    return switch (target.os.tag) {
        .windows => b.fmt("{s}-windows.dll", .{arch}),
        .macos => b.fmt("{s}-macos.dylib", .{arch}),
        .linux => b.fmt("{s}-linux.so", .{arch}),
        else => @panic("unsupported os"),
    };
}

fn getIncludeDir(os: std.Target) []const u8 {
    return switch (os.os.tag) {
        .linux => "linux",
        .macos => "darwin",
        .windows => "win32",
        else => @panic("unsupported os"),
    };
}

fn findJavaInclude(b: *std.Build, target: std.Target) !std.Build.LazyPath {
    const host = b.graph.host.result;

    if (target.os.tag == host.os.tag) {
        if (b.graph.env_map.get("JAVA_HOME")) |java| {
            const path = try std.fs.path.resolve(b.allocator, &.{ java, "include" });
            return .{ .cwd_relative = path };
        }
        std.log.warn("Couldn't find host headers. Using local headers instead", .{});
    }

    return b.path("jni-headers");
}

pub fn build(b: *std.Build) !void {
    const target = b.standardTargetOptions(.{ .whitelist = platforms });
    b.getInstallStep().dependOn(try buildFor(b, target));

    const step = b.step("all", "Build for all platforms");
    for (platforms) |query| step.dependOn(try buildFor(b, b.resolveTargetQuery(query)));
}

fn buildFor(b: *std.Build, target: std.Build.ResolvedTarget) !*std.Build.Step {
    const include: std.Build.LazyPath = try findJavaInclude(b, target.result);

    const mod = b.createModule(.{
        .optimize = .ReleaseSafe,
        .target = target,
        .link_libcpp = true,
    });

    const output = getOutputName(b, target.result);
    const header = getIncludeDir(target.result);

    const lib = b.addLibrary(.{
        .linkage = .dynamic,
        .name = "$projectname",
        .root_module = mod,
    });

    mod.addIncludePath(include);
    mod.addIncludePath(try include.join(b.allocator, header));
    try mod.c_macros.append(b.allocator, "$definitions");

    mod.addCSourceFiles(.{
        .root = b.path("cpp"),
        .language = .cpp,
        .files = &.{
            // MAIN_FILES
            $mainfiles
            // CLASS_FILES
            $classfiles
        },
        .flags = &.{ "-std=c++17", "-fno-sanitize=undefined" },
    });

    const artifact = b.addInstallArtifact(lib, .{
        .dest_sub_path = output,
        .dest_dir = .{ .override = .prefix },
    });

    return &artifact.step;
}
