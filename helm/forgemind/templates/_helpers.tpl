{{- define "forgemind.labels" -}}
app.kubernetes.io/name: forgemind
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "forgemind.selectorLabels" -}}
app.kubernetes.io/name: forgemind
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
