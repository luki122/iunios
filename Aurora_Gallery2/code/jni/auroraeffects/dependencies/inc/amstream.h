/*----------------------------------------------------------------------------------------------
*
* This file is ArcSoft's property. It contains ArcSoft's trade secret, proprietary and 		
* confidential information. 
* 
* The information and code contained in this file is only for authorized ArcSoft employees 
* to design, create, modify, or review.
* 
* DO NOT DISTRIBUTE, DO NOT DUPLICATE OR TRANSMIT IN ANY FORM WITHOUT PROPER AUTHORIZATION.
* 
* If you are not an intended recipient of this file, you must not copy, distribute, modify, 
* or take any action in reliance on it. 
* 
* If you have received this file in error, please immediately notify ArcSoft and 
* permanently delete the original and any copy of any file and any printout thereof.
*
*-------------------------------------------------------------------------------------------------*/

#ifndef __AMSTREAM_H__
#define __AMSTREAM_H__

#include "amcomdef.h"



typedef		MVoid*	HMSTREAM;

#define		INVALID_HMSTREAM  MNull

#define		STREAM_BEGIN	0
#define		STREAM_END		1
#define		STREAM_CUR		2


#define		STREAM_READ			0x0001 
#define		STREAM_WRITE		0x0002 
#define		STREAM_APPEND		0x0003
#define		STREAM_R_PLUS		0x0004
#define		STREAM_W_PLUS		0x0005
#define		STREAM_A_PLUS		0x0006
#define		STREAM_ASYNC_WRITE	0x0007 

//add by zdzhang
#ifndef STREAM_DRM_READ
#define	STREAM_DRM_READ			0x0101
#endif
//add by zdzhang

#define		STREAM_CONSUME_NORMAL   0x0001
#define		STREAM_CONSUME_PREVIEW  0x0002


#ifdef 		_PALM_OS_

typedef struct 
{	
	MChar *	name;
	MDWord	type;
	MDWord	creator;
	MWord	cardNo;	
	MByte	system;		
	MByte	reserved;
}FILEPARA, *LPFILEPARA;


#define MFILE_DATABASE		0
#define MFILE_VFS			1

#endif//_PALM_OS_



#ifdef __cplusplus
extern "C" {
#endif

#ifdef		M_WIDE_CHAR 
	#define	MStreamOpenFromFile		MStreamOpenFromFileW
	#define	MStreamOpenFromDRMFile	MStreamOpenFromDRMFileW
	#define	MStreamFileCreate		MStreamFileCreateW
	#define	MStreamFileDelete		MStreamFileDeleteW
	#define	MStreamFileRename		MStreamFileRenameW
	#define	MStreamFileCopy			MStreamFileCopyW
	#define	MStreamFileMove			MStreamFileMoveW
	#define	MStreamFileGetSize		MStreamFileGetSizeW
	#define	MStreamFileExists		MStreamFileExistsW
#else
	#define	MStreamOpenFromFile		MStreamOpenFromFileS
	#define	MStreamOpenFromDRMFile	MStreamOpenFromDRMFileS
	#define	MStreamFileCreate		MStreamFileCreateS
	#define	MStreamFileDelete		MStreamFileDeleteS
	#define	MStreamFileRename		MStreamFileRenameS
	#define	MStreamFileCopy			MStreamFileCopyS
	#define	MStreamFileMove			MStreamFileMoveS
	#define	MStreamFileGetSize		MStreamFileGetSizeS
	#define	MStreamFileExists		MStreamFileExistsS
#endif 


HMSTREAM	MStreamOpenFromFileS(const MVoid *file_para, MWord mode);
HMSTREAM	MStreamOpenFromFileW(const MVoid *file_para, MWord mode);


HMSTREAM    MStreamOpenFromDRMFileS(const MVoid* file_para, MWord wMode, MDWord dwConsumeType, MVoid* pDrmInfo);
HMSTREAM    MStreamOpenFromDRMFileW(const MVoid* file_para, MWord wMode, MDWord dwConsumeType, MVoid* pDrmInfo);


 


HMSTREAM	MStreamOpenFromMemoryBlock(MVoid* pMem, MLong lMemSize);

MBool		MStreamFileExistsS(const MVoid* file_para);
MBool		MStreamFileExistsW(const MVoid* file_para);

HMSTREAM	MStreamFileCreateS(const MVoid* file_para);
HMSTREAM	MStreamFileCreateW(const MVoid*	file_para);

MBool		MStreamFileDeleteS(const MVoid* file_para);
MBool		MStreamFileDeleteW(const MVoid* file_para);

MBool		MStreamFileRenameS(const MVoid* old_file_para, const MVoid* new_file_para);
MBool		MStreamFileRenameW(const MVoid* old_file_para, const MVoid* new_file_para);


MBool		MStreamFileCopyS(const MVoid* dst_file_para, const MVoid* src_file_para, MBool bFailIfExists);
MBool		MStreamFileCopyW(const MVoid* dst_file_para, const MVoid* src_file_para, MBool bFailIfExists);

MBool		MStreamFileMoveS(const MVoid* dst_file_para, const MVoid* src_file_para);
MBool		MStreamFileMoveW(const MVoid* dst_file_para, const MVoid* src_file_para);

MLong		MStreamFileGetSizeS(const MVoid* file_para);
MLong		MStreamFileGetSizeW(const MVoid* file_para);

MLong		MStreamGetSize(HMSTREAM hStream);
MLong		MStreamSetSize(HMSTREAM hStream, MLong lSize);

MLong		MStreamRead(HMSTREAM hStream, MVoid* pBuf, MLong lSize);
MLong		MStreamWrite(HMSTREAM hStream, MVoid* pBuf, MLong lSize);
MBool		MStreamFlush(HMSTREAM hStream);
MLong		MStreamSeek(HMSTREAM hStream, MShort start, MLong lOffset);
MLong		MStreamTell(HMSTREAM hStream);
MLong		MStreamCopy(HMSTREAM hSrcStream, HMSTREAM hDstStream, MLong lWriteSize);
MBool		MStreamClose(HMSTREAM hStream);




#ifdef M_DEBUG
#include "trace\amstreamd.h"
#endif

#ifdef __cplusplus
}
#endif


#endif

