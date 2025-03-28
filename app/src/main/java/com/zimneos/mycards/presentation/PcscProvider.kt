package com.zimneos.mycards.presentation

import android.nfc.tech.IsoDep
import com.github.devnied.emvnfccard.exception.CommunicationException
import com.github.devnied.emvnfccard.parser.IProvider
import java.io.IOException

class PcscProvider : IProvider {

    private lateinit var mTagCom: IsoDep

    @Throws(CommunicationException::class)
    override fun transceive(pCommand: ByteArray?): ByteArray? {
        var response: ByteArray? = null
        try {
            if (mTagCom.isConnected) {
                response = mTagCom.transceive(pCommand)
            }
        } catch (e: IOException) {
            throw CommunicationException(e.message)
        }
        return response
    }

    override fun getAt(): ByteArray {
        var result: ByteArray?
        result = mTagCom.historicalBytes
        if (result == null) {
            result = mTagCom.hiLayerResponse
        }
        return result
    }

    fun setmTagCom(mTagCom: IsoDep) {
        this.mTagCom = mTagCom
    }
}