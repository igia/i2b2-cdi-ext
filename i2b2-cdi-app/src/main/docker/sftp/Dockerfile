FROM alpine:latest

ENV SFTP_USER igia_sftp
ENV SFTP_PASSWORD h1psftpPwd
ENV SFTP_HOME /data

# Install OpenSSH and
# ensure key creation
RUN apk add --no-cache openssh && \
    rm -rf /etc/ssh/ssh_host_rsa_key /etc/ssh/ssh_host_dsa_key /etc/ssh/ssh_host_ecdsa_key

# Copy entrypoint script
COPY entrypoint.sh /

# SSH Server configuration file
COPY sshd_config /etc/ssh/sshd_config

EXPOSE 22

# Run entrypoint
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]