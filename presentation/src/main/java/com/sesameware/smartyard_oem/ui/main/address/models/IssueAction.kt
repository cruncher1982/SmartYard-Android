package com.sesameware.smartyard_oem.ui.main.address.models

sealed interface IssueAction

data class OnIssueClick(val issue: IssueModel) : IssueAction
data object OnQrCodeClick : IssueAction