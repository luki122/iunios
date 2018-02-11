#!/bin/bash
NDK=$NDKROOT 
SYSROOT=$NDK/platforms/android-19/arch-arm
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
CPU=arm
PREFIX=$(pwd)/android/$CPU 
ADDI_CFLAGS="-marm"
function build_one
{
./configure \
	--prefix=$PREFIX \
	--enable-cross-compile \
	--enable-small \
	--extra-libs="-lgcc" \
	--cc=$TOOLCHAIN/bin/arm-linux-androideabi-gcc \
	--disable-shared \
	--disable-doc \
	--disable-ffmpeg \
	--disable-ffplay \
	--disable-ffprobe \
	--disable-ffserver \
	--disable-avdevice \
	--disable-symver \
	--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
	--target-os=linux \
	--arch=arm \
	--enable-cross-compile \
   	--sysroot=$SYSROOT \
	--extra-cflags="-Os -fpic $ADDI_CFLAGS" \
	--extra-ldflags="$ADDI_LDFLAGS" \
	--disable-decoders \
	--enable-decoder=ape \
	--enable-decoder=mp3 \
	--enable-decoder=mp2 \
	--enable-decoder=flac \
	--disable-encoders \
	--disable-muxers \
	--disable-demuxers \
	--enable-demuxer=ape \
	--enable-demuxer=mp3 \
	--enable-demuxer=mp2 \
	--enable-demuxer=flac \
	--disable-parsers \
	--enable-avresample \
	--disable-indevs \
	--disable-debug \
	--disable-swscale  \
	--disable-swscale-alpha \
	--enable-network \
	--enable-protocols \
	--enable-protocol=file \
	--disable-postproc \
	--disable-avfilter \
	--disable-filters \
    	$ADDITIONAL_CONFIGURE_FLAG
make clean
make -j4
make install 
}
build_one

$TOOLCHAIN/bin/arm-linux-androideabi-ar d libavcodec/libavcodec.a inverse.o

$TOOLCHAIN/bin/arm-linux-androideabi-ld -rpath-link=$SYSROOT/usr/lib -L$SYSROOT/usr/lib  -soname libaudiocodec.so -shared -nostdlib  -z noexecstack -Bsymbolic --whole-archive --no-undefined -o $PREFIX/libaudiocodec.so libavcodec/libavcodec.a libavformat/libavformat.a libavutil/libavutil.a libswresample/libswresample.a libavresample/libavresample.a -lc -lm -lz -ldl -llog  --dynamic-linker=/system/bin/linker $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9/libgcc.a

$TOOLCHAIN/bin/arm-linux-androideabi-strip android/arm/libaudiocodec.so
cp config.h android/arm/.
cp config.log android/arm/.

