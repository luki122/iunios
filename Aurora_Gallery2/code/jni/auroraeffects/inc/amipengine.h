#include "amcomdef.h"
#include "amdisplay.h"
#include "asvloffscreen.h"

//For MIPDoEffect
#define MEID_FLEETINGTIME				0x0000
#define MEID_CRAYON						0x0001
#define MEID_SNOWFLAKES                 0x0002
#define MEID_LIGHTBEAM					0x0003
#define MEID_REFLECTION					0x0004
#define MEID_SUNSET						0x0005
#define MEID_REVERSAL					0x0006
#define MEID_WARMLOMO					0x0007
#define MEID_COLDLOMO					0x0008
#define MEID_SOFTPINK					0x0009
#define MEID_JAPANBACKLIGHT				0x000a
#define MEID_COSMETOLOGY_BACKLIGHT		0x000b

//For dwDirection
#define MDIRECTION_FLIP_HORIZONTAL                0x100

typedef struct __tag_effectparam
{
	MDWord	dwEffectID;
	MDWord  dwDirection;
	MVoid	*pEffect;
	MDWord	dwParamSize;
}MEffectParam;

typedef struct __tag_pixelinfo
{
	MDWord	dwPixelArrayFormat;
	MLong	lWidth;
	MLong	lHeight;
}MPixelInfo;

typedef struct __tag_cosmetologybackligntparam 
{
	MLong	lDermabrasionSize;			//>0
	MDouble dDermabrasionStrength;		//0-100
	MLong	lWhiteningStrength;			//0-100
}CosmetologyBackligntParam;

#ifdef __cplusplus
extern "C"
{
#endif

MRESULT MIPCreateImageEngine(MTChar *MaskPath,MEffectParam *peffectparam,MPixelInfo *ppixelinfo,MHandle *phandle);

MRESULT MIPDoEffect(MHandle handle, LPASVLOFFSCREEN pOffscreen);

MRESULT MIPDestroyImageEngine(MHandle handle);

#ifdef __cplusplus
};
#endif